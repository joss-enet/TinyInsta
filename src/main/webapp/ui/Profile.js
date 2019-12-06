var m = require("mithril")

module.exports = {

	oninit: function() {
		m.request({
			method: "GET",
		    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+m.route.param("pseudo"),
			}).then(function(result) {
				data = result;
		});
		user = sessionStorage.getItem("user");
	},
	
	view: function() {
		
		return m("div.content-wrapper", [
				m("h1", "Profile of "+m.route.param("pseudo")),
				m("button.button.post[type=button][id=follow-button]", {
					onclick: function(e) {
						
						if (document.getElementById("follow-button").textContent == "Follow") {
							m.request({
								method: "PUT",
							    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/follow?follower="+user+"&followed="+m.route.param("pseudo"),
								}).then(function(result) {
									if (result.key.name == "ok") {
										document.getElementById("follow-button").textContent = "Unfollow";
							    	} else {
							    		alert("Erreur.");
							    	}
							});

						} else {
							m.request({
								method: "DELETE",
							    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/follow?follower="+user+"&followed="+m.route.param("pseudo"),
								}).then(function(result) {
									if (result.key.name == "ok") {
										document.getElementById("follow-button").textContent = "Follow";
							    	} else {
							    		alert("Erreur.");
							    	}
							});
						}
					}
				}, "Follow")
			]);
	}
}