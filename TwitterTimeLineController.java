package com.twittertimeline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.TimelineOperations;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import statemachine.StateMachine;
import statemachine.tweettohtml.CaptureTag;
import costatemachine.tweettohtml.CheckHttpAction;
import statemachine.tweettohtml.DefaultAction;
import statemachine.tweettohtml.ReadyAction;
import statemachine.tweettohtml.TweetState;
import statemachine.tweettohtml.strategy.HashTagStrategy;
import statemachine.tweettohtml.strategy.UrlStrategy;
import statemachine.tweettohtml.strategy.UserNameStrategy;


@Controller
public class TwitterTimeLineController {

	private static final Logger logger = LoggerFactory
			.getLogger(TwitterTimeLineController.class);

	private final Twitter twitter;

	@Autowired
	public TwitterTimeLineController(Twitter twitter) {
		this.twitter = twitter;
	}

	@RequestMapping(value = "timeline", method = RequestMethod.GET)
	public String getUserTimeline(@RequestParam("id") String screenName, Model model) {

		logger.info("Loading Twitter timeline for :" + screenName);

		List<Tweet> results = queryForTweets(screenName);

		// Etapa Opcional  - formatação dos Tweets em HTML
		formatTweets(results);

		model.addAttribute("tweets", results);
		model.addAttribute("id", screenName);

		return "timeline";
	}

	private List<Tweet> queryForTweets(String screenName) {

		TimelineOperations timelineOps = twitter.timelineOperations();
		List<Tweet> results = timelineOps.getUserTimeline(screenName);
		logger.info("Fond Twitter timeline for :" + screenName + " adding " + results.size()
				+ " tweets to model");
		return results;
	}

	private void formatTweets(List<Tweet> tweets) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StateMachine<TweetState> stateMachine = createStateMachine(bos);

		for (Tweet tweet : tweets) {

			bos.reset();
			String text = tweet.getText();
			stateMachine.processStream(new ByteArrayInputStream(text.getBytes()));

			String out = bos.toString();
			tweet.setText(out);
		}
	}

	private StateMachine<TweetState> createStateMachine(ByteArrayOutputStream bos) {

		StateMachine<TweetState> machine = new StateMachine<TweetState>(TweetState.OFF);

		// Adicionando  algumas ações à máquina de estado //

		machine.addAction(TweetState.OFF, new DefaultAction(bos));
		machine.addAction(TweetState.RUNNING, new DefaultAction(bos));
		machine.addAction(TweetState.READY, new ReadyAction(bos));
		machine.addAction(TweetState.HASHTAG, new CaptureTag(bos, new HashTagStrategy()));
		machine.addAction(TweetState.NAMETAG, new CaptureTag(bos, new UserNameStrategy()));
		machine.addAction(TweetState.HTTPCHECK, new CheckHttpAction(bos));
		machine.addAction(TweetState.URL, new CaptureTag(bos, new UrlStrategy()));

		return machine;
	}

}