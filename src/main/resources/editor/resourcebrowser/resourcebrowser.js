steal.plugins(
	'jquery/controller/view', 
	'jquery/view/tmpl')
	.models('../../models/page')
.views('//editor/resourcebrowser/views/init.tmpl')
.css('resourcebrowser')
.then('resourcescrollview', 'resourcelistview')
.then(function($) {

  $.Controller('Editor.Resourcebrowser',
	{
		defaults: {
			resources: {},
			resourceType: 'pages'
		}
	},
	{
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/init.tmpl', {});
			this._loadResources();					
		},
		
		_showResourceScrollView: function(pages) {
			this.options.resources = pages;
			this.find('#resourceview').editor_resourcescrollview({resources: pages});
		},
		
		_showResourceListView: function(pages) {
			this.options.resources = pages;
			this.find('#resourceview').editor_resourcelistview({resources: pages});
		},
		
		_loadResources: function() {
			if(this.options.resourceType == 'pages') {
				Page.findAll({}, this.callback('_showResourceScrollView'));
			} else if(this.options.resourceType == 'media') {
				steal.dev.log('load Media');
			}
		},
		
		"button.recent click": function(el, ev) {
			steal.dev.log('recent')
		},
		"button.favorites click": function(el, ev) {
			steal.dev.log('favorites')
		},
		"button.pending click": function(el, ev) {
			steal.dev.log('show pending')
		},
		"button.all click": function(el, ev) {
			steal.dev.log('show all')
		},
		"button.list click": function(el, ev) {
			this._showResourceListView(this.options.resources);
		},
		"button.thumbnails click": function(el, ev) {
			this._showResourceScrollView(this.options.resources)
		}
	});
});
