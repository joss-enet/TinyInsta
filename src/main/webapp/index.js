var m = require("mithril")

var Homepage = require("./ui/Homepage.js")
var Login = require("./ui/Login.js")
var Inscription = require("./ui/Inscription")
var Timeline = require("./ui/Timeline")

m.route(document.body, "/homepage", {
	"/homepage": Homepage,
	"/login": Login,
	"/inscription": Inscription,
	"/timeline": Timeline,
})