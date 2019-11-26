var m = require("mithril")

function encodeImageFileAsURL(element) {
	  var file = element.files[0];
	  var reader = new FileReader();
	  reader.onloadend = function() {
		  console.log('RESULT', reader.result)
	  }
	  reader.readAsDataURL(file);
}

module.exports = {
	view: function() {
		return m("form", {
				onsubmit: function(e) {
					if (pseudo!="") {
						e.preventDefault()
						m.request({
							method: "POST",
				            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+pseudo+"/posts?message="+message+"&image="+image,
						}).then(function(result) {
							var parsedResult = JSON.parse(result);
							console.log(parsedResult);
						    if (parsedResult.key.name != "ok") {
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
                onchange: function (e) {image = encodeImageFileAsURL(e.target)},
            }),
            m("label.label", "Message"),
            m("input.input[type=text][placeholder=message]", {
                oninput: function (e) {message = e.target.value},
            }),
            m("button.button[type=submit]",  "Poster"),
		])
	}
}