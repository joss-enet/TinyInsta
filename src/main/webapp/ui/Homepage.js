var m = require("mithril")

module.exports = {
	view: function() {
		return m("main", [
			m("h1", "Welcome to TinyInsta"),
			m("a", {href: "#!/login"}, "Se connecter"),
			m("br"),
			m("a", {href: "#!/inscription"}, "Créer un compte"),
			m("br"),
			m("a", {href: "#!/post"}, "Poster"),
		])
	}
}