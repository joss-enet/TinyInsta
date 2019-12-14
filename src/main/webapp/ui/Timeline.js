var m = require("mithril");

var data = {
		
	user: sessionStorage.getItem("user"),
    posts: [],
    buttonValue: [],
    offset: 0,
    moreMessages: true,
    
    loadPosts: function() {
        return m.request({
        	method: "GET",
            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/timeline?pseudo="+data.user+"&offset="+data.offset,
        	}).then(function(result) {
        		if (result.items.length < 10) {
        			data.moreMessages = false;
        		}
        		data.posts[data.offset] = result.items;
        		data.posts[data.offset].map(row => 
        			data.initLikeButton(row.properties.pseudo, row.properties.id)
        		);
        		data.offset = data.offset+1;
        	})
    },
    
    initLikeButton: function(poster, postID) {
    	return m.request({
	    		method: "GET",
	    	    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+data.user+"/likes/"+poster+"/"+postID,
	    		}).then(function(result) {
	    			if (result.key.name == "ok") {
	    				data.buttonValue[poster+postID] = "Unlike";
	    	    	} else {
	    	    		data.buttonValue[poster+postID] = "Like";
	    	    	}
	    		})
    },
    
    checkRemainingPosts: function() {
    	if (!data.moreMessages) {
			document.getElementById("plus-button").remove();
    	}
    }
   
}

module.exports = {
		
	oninit: data.loadPosts,
	
	onupdate: data.checkRemainingPosts,
		
	view: function() {
		
		//if the data is not fully loaded, show loading sign
		if (data.posts.length == 0) {
			return m("body", [
					m("div.top-bar", 
							m("form.search-bar", {
								onsubmit: function(e) {
									e.preventDefault()
									m.request({
										method: "GET",
							            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+search,
									}).then(function(result) {
										if (result.key.name != "not ok") {
									    	m.route.set("/profile/:pseudo", {pseudo: search} );
								    	} else {
									    	alert("Profil non trouvé.");
								    	}
									})
								}
				            }, [
				            	m("input.input[type=text][placeholder=Rechercher...]", {
				                    oninput: function (e) {search = e.target.value}
				                }),
				                m("button.button[type=submit]", "Rechercher")
				            ]),
							m("button.button.post[type=button]", {
								onclick: function(e) {m.route.set("/post");}
							}, "Poster")
						), 
						m("div.content-wrapper.timeline", 
							m("h1", "Timeline"),
							m("h2", "Loading...")
						)
				])
		}
	
		
		
		return m("main", [
			m("div.top-bar", 
				m("form.search-bar", {
					onsubmit: function(e) {
						e.preventDefault()
						m.request({
							method: "GET",
				            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+search,
						}).then(function(result) {
							if (result.key.name == search) {
						    	m.route.set("/profile/:pseudo", {pseudo: search} );
					    	} else {
						    	alert("Profil non trouvé");
					    	}
						})
					}
	            }, [
	            	m("input.input[type=text][placeholder=Rechercher...]", {
	                    oninput: function (e) {search = e.target.value}
	                }),
	                m("button.button[type=submit]", "Rechercher")
	            ]),
				m("button.button.post[type=button]", {
					onclick: function(e) {m.route.set("/post");}
				}, "Poster")
			), 
			m("div.content-wrapper.timeline", 
					m("h1", "Timeline"),
					m("table.timeline", 
						data.posts.map(group =>
							group.map(row => 
								m("tr.timeline-pattern",
									m("p.postPseudo", row.properties.pseudo),
									m("img", {src: row.properties.image.value}),
									m("p.postMessage", row.properties.message),
									m("p.postLikes[id="+row.properties.pseudo+row.properties.id+"likes]", row.properties.likes),
									m("button.button[type=button][id="+row.properties.pseudo+row.properties.id+"button]",{
										onclick: function(e) {
											
											if (document.getElementById(row.properties.pseudo+row.properties.id+"button").textContent == "Like") {
												m.request({
													method: "PUT",
												    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+row.properties.pseudo+"/posts/"+row.properties.id+"?liker="+data.user,
													}).then(function(result) {
														if (result.key.name == "ok") {
															document.getElementById(row.properties.pseudo+row.properties.id+"button").textContent = "Unlike";
															document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent = parseInt(document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent)+1;
												    	} else {
												    		alert("Erreur.");
												    	}
												});
					
											} else {
												m.request({
													method: "DELETE",
												    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+row.properties.pseudo+"/posts/"+row.properties.id+"?liker="+data.user,
													}).then(function(result) {
														if (result.key.name == "ok") {
															document.getElementById(row.properties.pseudo+row.properties.id+"button").textContent = "Like";
															document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent = parseInt(document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent)-1;
												    	} else {
												    		alert("Erreur.");
												    	}
												});
											}
											
										}
										
										
									}, data.buttonValue[row.properties.pseudo+row.properties.id])
								)
							)
						)
					),
					m("button.button[type=button][id=plus-button]", {
						onclick: function(e) {
							m.request({
					        	method: "GET",
					            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/timeline?pseudo="+data.user+"&offset="+data.offset,
					        	}).then(function(result) {
					        		if (result.items.length < 10) {
					        			document.getElementById("plus-button").remove();
					        		}
					        		data.posts[data.offset] = result.items;
					        		data.posts[data.offset].map(row => 
					        			data.initLikeButton(row.properties.pseudo, row.properties.id)
					        		);
					        		data.offset = data.offset+1;
					        	})
			        	}
					}, "Plus")
			)
		])
	}
}