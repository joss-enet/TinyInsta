var m = require("mithril")

module.exports = {
	view: function() {
		return m("main", [
			m("h1", "Welcome to TinyInsta"),
			m("button.button[type=button]", {
				onclick: function(e) {m.route.set("/login")}
			}, "Se connecter"),
			m("br"),
			m("button.button[type=button]", {
				onclick: function(e) {m.route.set("/inscription")}
			}, "S'inscrire"),
			m("br"),
			m("a", {href: "#!/post"}, "Poster"),
		])
	}
}