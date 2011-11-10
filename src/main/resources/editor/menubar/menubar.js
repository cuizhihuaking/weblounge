steal.plugins(
	'jquery/controller',
	'jquery/controller/view',
	'jquery/view',
	'jquery/view/tmpl',
	'editor/pagecreator',
	'editor/pageletcreator',
	'editor/pageheadeditor',
	'jqueryui/dialog',
	'jqueryui/datepicker',
	'jqueryui/draggable',
	'jqueryui/droppable',
	'jqueryui/resizable',
	'jqueryui/mouse',
	'jqueryui/button')
.views(
	'//editor/menubar/views/menubar.tmpl')
.css('menubar')
.then(function($) {

    $.Controller("Editor.Menubar",
    {
    	/**
    	 * Mode 0 = Designer
    	 * Mode 1 = Pages
    	 * Mode 2 = Media
    	 */
    	defaults: {
    		mode: 1,
    		site: {},
    		language: {}
    	}
	},
    /* @prototype */
    {
	    /**
	     * Initialize a new MenuBar controller.
	     */
        init: function(el) {
            $(el).html('//editor/menubar/views/menubar.tmpl', {runtime: this.options.runtime, current: this.options.language});
            this._updateView();
            this._initDialogs();
            $('#wbl-pageletcreator').editor_pageletcreator({language: this.options.language, runtime: this.options.runtime});
            this._initPageLocking();
        },
        
        update: function(options) {
        	if(options !== undefined) {
        		this.options.mode = options.mode;
        	}
        	this._updateView();
        },
        
        /**
         * Show selected Tab
         */
        _updateView: function() {
        	switch (this.options.mode) {
	      	  case 0:
	      		  this._toggleTab(this.find('.wbl-tab.wbl-designer'));
	      		  $(document.body).css('overflow', 'visible');
	      		  if(!this.disabled) {
	      			  this.find('img.wbl-add').show();
	      			  this.find('img.wbl-more').show();
	      			  this.find('img.wbl-pageSettings').show();
	      		  }
	      		  this.find('span.wbl-editmode').show();
	      		  this.find('span.wbl-language').show();
	      		  this.find('div#wbl-pageOptions').show();
	      		  break;
	      	  case 1:
	      		  this._toggleTab(this.find('.wbl-tab.wbl-pages'));
	      		  $(document.body).css('overflow', 'hidden');
	      		  this.find('img.wbl-add').hide();
	      		  this.find('img.wbl-more').hide();
	      		  this.find('img.wbl-pageSettings').hide();
	      		  this.find('span.wbl-language').hide();
	      		  this.find('span.wbl-editmode').hide();
	      		  this.find('div#wbl-pageOptions').hide();
	      		  window.scrollTo(0, 0);
	      		  break;
	      	  case 2:
	      		  this._toggleTab(this.find('.wbl-tab.wbl-media'));
	      		  $(document.body).css('overflow', 'hidden');
	      		  this.find('img.wbl-pageSettings').hide();
	      		  this.find('img.wbl-add').hide();
	      		  this.find('img.wbl-more').hide();
	      		  this.find('span.wbl-language').hide();
	      		  this.find('span.wbl-editmode').hide();
	      		  this.find('div#wbl-pageOptions').hide();
	      		  window.scrollTo(0, 0);
	      		  break;
        	}
        },
        
        _initDialogs: function() {
			this.userDialog = $('<div></div>')
			.load(this.options.runtime.getRootPath() + '/editor/menubar/views/user-dialog.html')
			.dialog({
				modal: true,
				title: 'Einstellungen',
				autoOpen: false,
				resizable: true,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: $.proxy(function () {
						//TODO Save User Settings
//						this.element.trigger('saveSettings');
						this.userDialog.dialog('close');
					},this)
				},
				open: function(event, ui) {
					$(document.body).css('overflow', 'hidden');
				},
				beforeClose: function(event, ui) {
					$(document.body).css('overflow', 'visible');
				}
			});
			
			this.publishDialog = $('<div></div>')
			.load(this.options.runtime.getRootPath() + '/editor/menubar/views/publish-dialog.html')
			.dialog({
				modal: true,
				title: 'Seite publizieren',
				autoOpen: false,
				resizable: false,
				buttons: {
					Nein: function() {
						$(this).dialog('close');
					},
					Ja: $.proxy(function () {
						this.options.page.publish($.proxy(function() {
							// Success nothing to do
						}, this), function(jqXHR, textStatus, errorThrown) {
							if(jqXHR.status == 412) {
								alert('Publish aborted: Page has invalid references!')
							} else {
								alert('Publish error: ' + errorThrown);
							}
						});
						this.publishDialog.dialog('close');
					},this)
				},
				open: function(event, ui) {
					$(document.body).css('overflow', 'hidden');
				},
				beforeClose: function(event, ui) {
					$(document.body).css('overflow', 'visible');
				}
			});
			
        },
        
        _initPageLocking: function() {
        	var locked = this.options.page.isLocked();
        	var userLocked = this.options.page.isLockedUser(this.options.runtime.getUserLogin())
        	var isAdmin = this.options.runtime.isSystemAdmin();
        	var lockCheckBox = $('input#wbl-editmode', this.element);
        	
        	if(locked && !userLocked) {
        		lockCheckBox.attr('disabled', 'disabled');
        		this._disableEditing();
        	} 
        	else if(locked && userLocked) {
        		lockCheckBox.removeAttr('disabled');
        		lockCheckBox.val(['editmode']);
        		this._enableEditing();
        		$('#wbl-pageletcreator').editor_pageletcreator();
        	} else {
        		lockCheckBox.removeAttr('disabled');
        		lockCheckBox.val([]);
        		this._disableEditing();
        	}
        	
        	if(lockCheckBox.is(':disabled') && isAdmin) {
        		lockCheckBox.removeAttr('disabled');
        		lockCheckBox.val([]);
        	}
        },
        
        /**
         * Delete a Cookie with setting expire to currentTime -1
         */
        _delete_cookie: function(cookieName) {
        	$.cookie(cookieName, null, { path: '/' });
        },
        
        _editorSelectionMode: function(editor, resourceMode, isMultiSelection, success) {
        	// Hide Dialog
        	this.editor = editor.parent().hide();
        	$(document).unbind('.dialog-overlay');
        	this.editorOverlay = $('body > div.ui-widget-overlay').hide();
        	// Open PageBrowser
        	
        	// Hide Menubar
        	this.element.find('a.wbl-designer').hide();
        	this.element.find('span.wbl-language').hide();
        	this.element.find('span.wbl-right').hide();
        	this.element.find('img.wbl-pageSettings').hide();
        	this.element.find('button.wbl-editorSelectionCancel, button.wbl-editorSelectionOK').button().show();
        	this.element.find('button.wbl-editorSelectionOK').button('option', 'disabled', true);
        	
        	var browser;
        	if(resourceMode == 'pages') {
        		this.element.find('a.wbl-media').hide();
        		this.element.find('a.wbl-pages').click();
        		browser = this.element.parent().find('#wbl-pagebrowser').editor_resourcebrowser('_enableEditorSelectionMode', isMultiSelection);
        	} else if(resourceMode == 'media') {
        		this.element.find('a.wbl-pages').hide();
        		this.element.find('a.wbl-media').click();
        		browser = this.element.parent().find('#wbl-mediabrowser').editor_resourcebrowser('_enableEditorSelectionMode', isMultiSelection);
        	} else if(resourceMode == 'file' || resourceMode == 'image' || resourceMode == 'movie') {
        		this.element.find('a.wbl-pages').hide();
        		this.element.find('a.wbl-media').click();
        		browser = this.element.parent().find('#wbl-mediabrowser').editor_resourcebrowser('_enableEditorSelectionMode', isMultiSelection, resourceMode);
        	}
        	
    		this.element.find('button.wbl-editorSelectionCancel').click($.proxy(function() { 
    			browser.editor_resourcebrowser('_disableEditorSelectionMode');
    			this._normalMode();
    			success(null);
    		}, this));
    		this.element.find('button.wbl-editorSelectionOK').click($.proxy(function() { 
    			browser.editor_resourcebrowser('_getSelection', function(selection) {
    				success(selection);
    			});
    			browser.editor_resourcebrowser('_disableEditorSelectionMode');
    			this._normalMode();
    		}, this));
        },
        
        _normalMode: function() {
        	this.editor.show();
        	this.editorOverlay.show();
        	
        	this.element.find('button.wbl-editorSelectionOK').unbind();
        	this.element.find('button.wbl-editorSelectionCancel').unbind();
        	
        	// Hide Cancel OK Button
        	this.element.find('button.wbl-editorSelectionCancel, button.wbl-editorSelectionOK').hide();
        	// Show Menubar
        	this.element.find('a.wbl-designer').show();
        	this.element.find('a.wbl-pages').show();
        	this.element.find('a.wbl-media').show();
        	this.element.find('span.wbl-language').show();
        	this.element.find('span.wbl-right').show();
        	this.element.find('img.wbl-pageSettings').show();
        	// Open Designer
        	this.element.find('a.wbl-designer').click();
        	$(document.body).css('overflow', 'hidden');
        },
        
        _enableEditing: function() {
        	this.disabled = false;
        	if(this.options.page.isWorkVersion())
        		$('.composer').editor_composer('enable');
        	$('#wbl-pageletcreator').editor_pageletcreator('enable');
//        	this.element.find('img.wbl-add').show();
//        	this.element.find('img.wbl-more').show();
        	this.element.find('img.wbl-pageSettings').show();
        },
        
        _disableEditing: function() {
        	this.disabled = true;
        	if(this.options.page.isWorkVersion())
        		$('.composer').editor_composer('disable');
        	$('#wbl-pageletcreator').editor_pageletcreator('disable');
//        	this.element.find('img.wbl-add').hide();
//        	this.element.find('img.wbl-more').hide();
        	this.element.find('img.wbl-pageSettings').hide();
        },
        
        _toggleTab: function(el) {
        	this.element.find('.wbl-tab.wbl-active').removeClass('wbl-active');
        	el.addClass('wbl-active');
        },
        
        ".wbl-tab click": function(el, ev) {
        	this._toggleTab(el);
        },
        
        ".wbl-tab.wbl-designer click": function(el, ev) {
        	el.trigger('showDesigner');
        },
        
		".wbl-tab.wbl-pages click": function(el, ev) {
			el.trigger('showPages');
		},
		
		".wbl-tab.wbl-media click": function(el, ev) {
			el.trigger('showMedia');
		},
		
		"li.wbl-settings click": function(el, ev) {
			$('.wbl-menu').hide();
			this.userDialog.dialog('open');
		},
		
		"li.wbl-publish click": function(el, ev) {
			$('.wbl-menu').hide();
			this.publishDialog.dialog('open');
		},
		
		"img.wbl-pageSettings click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-pageheadeditor').editor_pageheadeditor({page: this.options.page, language: this.options.language, runtime: this.options.runtime});
		},
		
		"li.wbl-news click": function(el, ev) {
			steal.dev.log('news')
		},
		
		"img.wbl-logout click": function(el, ev) {
			this._delete_cookie("weblounge.editor");
			var logouturl = '/system/weblounge/logout?path=';
			location.href = logouturl + location.pathname;
		},
		
		"li.wbl-newPage click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-pagecreator').editor_pagecreator({language: this.options.language, runtime: this.options.runtime});
		},
		
		"li.wbl-newUpload click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-massuploader').editor_massuploader({language: this.options.language, runtime: this.options.runtime});
		},
		
		"li.wbl-newNote click": function(el, ev) {
			steal.dev.log('note')
		},
		
		"li.wbl-newPagelet click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-pageletcreator').editor_pageletcreator();
		},
		
		"span.wbl-languageMenu img click": function(el, ev) {
			el.trigger('changeLanguage', el.attr('title'));
		},
		
		// trigger menus
		"img.wbl-add click": function(el, ev) {
			$('.wbl-menu').hide();
//			$('div#wbl-addMenu').show().hover(function() { }, function() {$(this).hide();});
			$('#wbl-pageletcreator').editor_pageletcreator();
		},
		
		"span.wbl-language click": function(el, ev) {
			$('.wbl-menu').hide();
			$('span.wbl-languageMenu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"img.wbl-more click": function(el, ev) {
			$('.wbl-menu').hide();
			$('div#wbl-moreMenu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"span.wbl-profileMenu click": function(el, ev) {
//			$('.wbl-menu').hide();
//			$('div#wbl-profileMenu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"div#wbl-pageOptions click": function(el, ev) {
			el.toggle(function() {
				$(this).animate({"right": "0"}, "slow")
			}, function() {
				$(this).animate({"right": "-200px"}, "slow")
			});
		},
		
		"input focus": function(el, ev) {
//			$('div#wbl-searchResult').show();
		},
		
		"input blur": function() {
			$('div#wbl-searchResult').hide();
		},
		
		"div.wbl-searchResult p.wbl-footer click": function() {
//			$('#editor').dialog( "option", "title", 'Suchresultate' ).dialog('open')
			$('div.wbl-searchResult').hide();
		},
		
		// trigger editmode
		"input#wbl-editmode click": function(el, ev) {
			ev.preventDefault();
			if(el.is(':checked')) {
				var isWorkVersion = this.options.page.isWorkVersion();
				this.options.page.lock(this.options.runtime.getUserLogin(), $.proxy(function() {
					$('input#wbl-editmode', this.element).val(['editmode']);
					// if version is life set page to work and init composer
					if(!isWorkVersion) {
						$('#weblounge-editor').editor_app('_initComposer', this.options.page);
					}
					this._enableEditing();
					$('#wbl-pageletcreator').editor_pageletcreator();
				}, this), $.proxy(function() {
					$('input#wbl-editmode', this.element).val([]);
					alert('Locking failed!');
				}, this));
			} else {
				this.options.page.unlock($.proxy(function() {
					$('input#wbl-editmode', this.element).val([]);
					this._disableEditing();
					this.publishDialog.dialog('open');
				}, this));
			}
		}
		
    });

});