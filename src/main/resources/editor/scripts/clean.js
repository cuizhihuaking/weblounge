//steal/js editor/scripts/compress.js

load("steal/rhino/steal.js");
steal.plugins('steal/clean',function(){
	steal.clean('editor/editor.html',{
		indent_size: 1, 
		indent_char: '\t', 
		jslint : false,
		ignore: /jquery\/jquery.js/,
		predefined: {
			steal: true, 
			jQuery: true, 
			$ : true,
			window : true
			}
	});
});
