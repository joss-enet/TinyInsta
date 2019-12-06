var m = require("mithril")

module.exports = {
	view: function() {
		return m("div.content-wrapper", [
					m("form", {
					onsubmit: function(e) {
						if (pseudo!="" && password!="") {
							e.preventDefault()
							m.request({
								method: "GET",
					            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/verify?pseudo="+pseudo+"&password="+password,
							}).then(function(result) {
								if (result.key.name == "ok") {
									sessionStorage.setItem("user", pseudo);
							    	m.route.set("/timeline");
						    	} else {
						    		alert("Identifiants invalides, veuillez réessayer.");
						    	}
						    })
						}
					}
				}, [
				m("h1", "Login"),
				m("label.label", "Pseudo"),
	            m("input.input[type=text][placeholder=pseudo]", {
	                oninput: function (e) {pseudo = e.target.value},
	            }),
	            m("label.label", "Mot de passe"),
	            m("input.input[type=password][placeholder=mot de passe]", {
	                oninput: function (e) {password = e.target.value},
	            }),
	            m("button.button[type=submit]",  "Se connecter"),
	            m("button.button[type=button]", {
					onclick: function(e) {m.route.set("/homepage")}
				}, "Retour")
			])
		])
	}
}