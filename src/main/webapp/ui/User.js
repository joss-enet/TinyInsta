var m = require("mithril")

var User = {
	current: {},
	
	save: function() {
		return m.request({
			method: "PUT",
			url: "http://localhost:8080/users/" + pseudo,
			body: User.current
		})
	}
}

module.exports = User