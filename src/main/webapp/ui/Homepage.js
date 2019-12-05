var m = require("mithril")

module.exports = {
	view: function() {
		return m("div.content-wrapper", [
				m("main.homepage", [
				m("h1", "Welcome to TinyInsta"),
				m("button.button[type=button]", {
					onclick: function(e) {m.route.set("/login")}
				}, "Se connecter"),
				m("br"),
				m("button.button[type=button]", {
					onclick: function(e) {m.route.set("/inscription")}
				}, "S'inscrire")
			])
		])
	}
}