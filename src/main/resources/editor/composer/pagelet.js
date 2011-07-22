steal.plugins('jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/droppable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.models('../../models/workbench',
		'../../models/pagelet')
.resources('trimpath-template')
.css('validation')
.then('inputconverter')
.then(function($) {

  $.Controller("Editor.Pagelet",
  /* @static */
  {
  },

  /* @prototype */
  {
    /**
     * Initialize a new Pagelet controller.
     */
    init: function(el) {
		this.element.attr('index', this.element.index());
		this.showHover = true;
    },
    
    update: function(options) {
    	if(options === undefined) return;
    	this.element.attr('index', this.element.index());
    	this.options.composer = options.composer;
    	this.showHover = true;
    },
    
    enable: function() {
    	this.showHover = true;
    },
    
    disable: function() {
    	this.showHover = false;
    },
    
    _openPageEditor: function(pageletEditor, isNew) {
    	// Parse Pagelet-Editor Data
    	var pagelet = this.options.composer.page.getEditorPagelet(this.options.composer.id, this.element.index(), this.options.composer.language);
    	var renderer = $(pageletEditor).find('renderer:first').text().trim();
    	var editor = $(pageletEditor).find('editor:first');
    	
    	if(!editor.length) {
    		this.element.html(this._processTemplate(renderer, pagelet));
    		return;
    	}
    	
    	// Load Pagelet CSS
    	$(pageletEditor).find('link').each(function(index) {
    		var test = $('head link[href="' + $(this).attr('href') + '"][rel="stylesheet"]');
    		if(test.length > 0) return;
    		$("head").append("<link>");
    	    var css = $("head").children(":last");
    	    css.attr({
    	      rel:  "stylesheet",
    	      type: $(this).attr('type'),
    	      href: $(this).attr('href')
    	    });
    	});
    	
    	// Load Pagelet Javascript
    	$(pageletEditor).find('script').each(function(index) {
    		$.getScript($(this).attr('src'));
    	});
    	
    	// Process Editor Template
    	var result  = this._processTemplate(editor.text(), pagelet);
    	
    	// Hack to create new Dom element
    	var resultDom = $('<div></div>').html(result);
    	this._convertInputs(resultDom, pagelet);
    	
		this.editorDialog = $('#pageleteditor').html('<form id="validate" onsubmit="return false;">' + resultDom.html() + '</form>')
		.dialog({
			title: 'Pagelet bearbeiten',
			width: 900,
			height: 800,
			modal: true,
			autoOpen: true,
			resizable: true,
			buttons: {
				Abbrechen: function() {
					$(this).dialog('close');
				},
				OK: $.proxy(function () {
					this.editorDialog.find("form#validate").submit();
					if(!this.editorDialog.find("form#validate").valid()) return;
					var newPagelet = this._updatePageletValues(pagelet);
					
					// Render site
					this.element.html(this._processTemplate(renderer, newPagelet));
					
					// Save New Pagelet
			    	this.options.composer.page.insertPagelet(newPagelet, this.options.composer.id, this.element.index());
			    	
					this.editorDialog.dialog('destroy');
				}, this)
			},
			close: $.proxy(function () {
				if(isNew == true) {
					this.options.composer.page.deletePagelet(this.options.composer.id, this.element.index());
					var composer = this.element.parent();
					this.element.remove();
					composer.editor_composer();
				}
				this.editorDialog.dialog('destroy');
			}, this)
		});
		this.editorDialog.find("form#validate").validate();
    },
    
    /**
     * Process a TrimPath template
     */
    _processTemplate: function(template, data) {
		var templateObject = TrimPath.parseTemplate(template);
		return templateObject.process(data);
    },
    
    /**
     * Converts editor input fields with Trimpath syntax
     */
    _convertInputs: function(editor, pagelet) {
    	$(editor).find(':input').each(function(index) {
    		var element = $(this).attr('name').split(':')
    		
    		if($(this).attr('type') == 'text' || $(this).attr('type') == 'hidden') {
    			InputConverter.convertText($(this), element, pagelet);
    		}
    		else if($(this).attr('type') == 'checkbox') {
    			InputConverter.convertCheckbox($(this), element, pagelet);
    		}
    		else if($(this).attr('type') == 'radio') {
    			InputConverter.convertRadio($(this), element, pagelet);
    		}
    		else if(this.tagName == 'TEXTAREA') {
    			InputConverter.convertTextarea($(this), element, pagelet);
    		}
    		else if(this.tagName == 'SELECT') {
    			InputConverter.convertSelect($(this), element, pagelet);
    		}
    	});
    },
    
    /**
     * Update the pagelet values from the dialog inputs and set the "current"
     */
    _updatePageletValues: function(pagelet) {
		var allInputs = this.editorDialog.find(':input');
		
		if(pagelet.locale.current == undefined) {
			pagelet = this._createNewLocale(pagelet, this.options.composer.language);
		}
		
		$.each(allInputs, function(i, input) {
			var element = input.name.split(':')
    		if(input.type == 'select-one' || input.type == 'select-multiple') {
    			var optionArray = new Array();
    			$(input).find('option:selected').each(function(){
    				optionArray.push($(this).val());
    			});
    			if($.isEmptyObject(optionArray)) return;
    			if(element[0] == 'property') {
    				pagelet.properties.property[element[1]] = optionArray.toString();
    			} 
    			else if(element[0] == 'element') {
    				pagelet.locale.current.text[element[1]] = optionArray.toString();
    			}
    		}
    		else if(input.type == 'checkbox') {
    			if(element[0] == 'property') {
					pagelet.properties.property[element[1]] = input.checked ? "true" : "false";
    			} 
    			else if(element[0] == 'element') {
					pagelet.locale.current.text[element[1]] = input.checked ? "true" : "false";
    			}
    		}
    		else if(input.type == 'radio') {
    			if(input.checked == false) return;
    			if(element[0] == 'property') {
    				pagelet.properties.property[element[1]] = input.value;
    			} 
    			else if(element[0] == 'element') {
    				pagelet.locale.current.text[element[1]] = input.value;
    			}
    		}
    		else {
    			if(input.value == '') return;
    			if(element[0] == 'property') {
    				pagelet.properties.property[element[1]] = input.value;
    			} 
    			else if(element[0] == 'element') {
    				pagelet.locale.current.text[element[1]] = input.value;
    			}
    		}
		});
		return pagelet;
    },
    
    /**
     * Create a new locale
     */
    _createNewLocale: function(pagelet, language) {
    	pagelet.locale.current = {};
    	pagelet.locale.current.text = {};
    	pagelet.locale.current.language = language;
    	pagelet.locale.current.original = false;
    	if($.isEmptyObject(pagelet.locale.original)) {
    		pagelet.locale.current.original = true;
    	}
		pagelet.locale.current.modified = {};
		pagelet.locale.current.modified.user = {};
		pagelet.locale.current.modified.user.id = this.options.composer.runtime.getUserLogin();
		pagelet.locale.current.modified.user.name = this.options.composer.runtime.getUserName();
		pagelet.locale.current.modified.user.realm = this.options.composer.runtime.getId();
		pagelet.locale.current.modified.date = new Date();
    	pagelet.locale.push(pagelet.locale.current);
		return pagelet;
    },

	'hoverenter': function(ev, hover) {
		if(this.showHover) 
			this.element.append('<img class="icon_editing" src="/weblounge/editor/composer/resources/icon_editing.png" />');
    },

	'hoverleave': function(ev, hover) {
		if(this.showHover) 
			this.element.find('img.icon_editing').remove();
    },

	'img.icon_editing click': function(ev) {
		Workbench.findOne({ id: this.options.composer.page.value.id, composer: this.options.composer.id, pagelet: this.element.index() }, this.callback('_openPageEditor'));
	}

  });

});
