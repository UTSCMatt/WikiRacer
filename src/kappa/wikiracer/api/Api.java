package kappa.wikiracer.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kappa.wikiracer.wiki.LinkRequest;

@RestController
public class Api {

	@RequestMapping(value = "/api/test/", method = RequestMethod.GET)
	public String test() {
		return "test";
	}

	@RequestMapping(value = "/api/test/wiki", method = RequestMethod.GET)
	public String wiki(@RequestParam(value = "title", defaultValue = "Albert Einstein") String title) {
		return LinkRequest.sendRequest(title);
	}
}
