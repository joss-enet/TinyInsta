var m = require("mithril")

module.exports = {
	view: function() {
		return m("form", {
				onsubmit: function(e) {
					e.preventDefault()
					m.request({
						method: "GET",
			            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+pseudo,
					}).then(function(result) {
					    if (result!=null) {
					    	m.route.set("/timeline");
				    	} else {
					    	m.route.set("/loginFailed");
				    	}
				    })
				}
			}, [
			m("h1", "Login"),
			m("label.label", "Pseudo"),
            m("input.input[type=text][placeholder=pseudo]", {
                oninput: function (e) {pseudo = e.target.value},
            }),
            m("button.button[type=submit]",  "Se connecter"),
		])
	}
}