var m = require("mithril")

module.exports = {
	view: function() {
		return m("form", {
			onsubmit: function(e) {
				e.preventDefault()
				m.request({
					method: "POST",
		            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users?nom="+nom+"&prenom="+prenom+"&pseudo="+pseudo+"&password="+password,
				}).then(function(result) {
					var parsedResult = JSON.parse(result);
					console.log(parsedResult);
				    if (parsedResult.key.name != null) {
				    	m.route.set("/timeline");
			    	} else {
				    	m.route.set("/loginFailed");
			    	}
				})
			}
		}, [
			m("h1", "Inscription"),
			m("label.label", "Pseudo"),
            m("input.input[type=text][placeholder=pseudo]", {
                oninput: function (e) {pseudo = e.target.value},
            }),
            m("label.label", "Nom"),
            m("input.input[type=text][placeholder=nom]", {
                oninput: function (e) {nom = e.target.value},
            }),
            m("label.label", "Prénom"),
            m("input.input[type=text][placeholder=prenom]", {
                oninput: function (e) {prenom = e.target.value},
            }),
            m("label.label", "Mot de passe"),
            m("input.input[type=text][placeholder=mot de passe]", {
                oninput: function (e) {password = e.target.value},
            }),
            m("button.button[type=submit]",  "S'inscrire"),
		])
	}
}
