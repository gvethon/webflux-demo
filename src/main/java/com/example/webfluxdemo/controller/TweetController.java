package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.model.Tweet;
import com.example.webfluxdemo.repository.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class TweetController {

	private final Logger logger = LoggerFactory.getLogger(TweetController.class);

	@Autowired
	private TweetRepository tweetRepository;

	@GetMapping("/tweets")
	public Flux<Tweet> getAllTweets() {
		return tweetRepository.findAll();
	}

	@GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Tweet> streamAllTweets() {
		return tweetRepository.findAll();
	}

	@PostMapping("/tweet")
	public Mono<Tweet> createTweet(@Valid @RequestBody Tweet tweet) {
		return tweetRepository.save(tweet);
	}

	@GetMapping("/tweets/{id}")
	public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable("id") String id) {
		return tweetRepository.findById(id)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PutMapping("/tweets/{id}")
	public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable("id") String tweetId, @Valid @RequestBody Tweet tweet) {
		return tweetRepository.findById(tweetId)
				.flatMap(existingTweet -> {
					existingTweet.setText(tweet.getText());
					return tweetRepository.save(existingTweet);
				})
				.map(updatedTweet -> new ResponseEntity<>(updatedTweet, HttpStatus.OK))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	public Mono<ResponseEntity<Void>> deleteTweet(@PathVariable("id") String tweetId) {
		return tweetRepository.findById(tweetId)
				.flatMap(existingTweet ->
						tweetRepository.delete(existingTweet)
								.then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
				)
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
