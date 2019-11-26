var m = require("mithril")


module.exports = {
	view: function() {
		return m("form", {
				onsubmit: function(e) {
					if (pseudo!="") {
						e.preventDefault()
						m.request({
							method: "POST",
				            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+pseudo+"/posts",
				            body: {
				            	"image": reader.result,
				            	"message": message
				            }
						}).then(function(result) {
						    if (result.key.name == "ok") {
						    	m.route.set("/homepage");
					    	} else {
						    	m.route.set("/loginFailed");
					    	}
					    })
					}
				}
			}, [
			m("h1", "Post"),
			m("label.label", "Pseudo"),
            m("input.input[type=text][placeholder=pseudo]", {
                oninput: function (e) {pseudo = e.target.value},
            }),
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
		])
	}
}