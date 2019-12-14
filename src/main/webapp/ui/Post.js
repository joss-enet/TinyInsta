var m = require("mithril");


module.exports = {
	view: function() {
		return m("div.content-wrapper.centered", [
					m("form", {
					onsubmit: function(e) {
						e.preventDefault();
						m.request({
							method: "POST",
				            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+sessionStorage.getItem("user")+"/posts",
				            body: {
				            	"image": reader.result,
				            	"message": message
				            }
						});
						alert("Message posté.");
					}
				}, [
				m("h1", "Post"),
	            m("label.label", "Image"),
	            m("input.input[type=file][accept=image/*][placeholder=mot de passe]", {
	                onchange: function (e) {
	                	var file = e.target.files[0];
	                	reader = new FileReader();
	                	reader.onloadend = function() {
	                		console.log("load ", reader.result);
	              	  	}
	                	reader.readAsDataURL(file);
	                },
	            }),
	            m("label.label", "Message"),
	            m("input.input[type=text][placeholder=message]", {
	                oninput: function (e) {message = e.target.value},
	            }),
	            m("button.button[type=submit]",  "Poster"),
	            m("button.button[type=button]", {
					onclick: function(e) {m.route.set("/timeline")}
				}, "Retour")
			])
		])
	}
}