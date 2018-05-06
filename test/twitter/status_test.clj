(ns twitter.status-test
  (:require [clojure.test :refer [deftest is]]
            [org.httpkit.client :as http]
            [twitter.oauth :as oauth]
            [twitter.core :as twitter]))

(def config
  {:consumer-key    (System/getenv "TWITTER_CONSUMER_KEY")
   :consumer-secret (System/getenv "TWITTER_CONSUMER_SECRET")
   :token           (System/getenv "TWITTER_TOKEN")
   :token-secret    (System/getenv "TWITTER_TOKEN_SECRET")})

(deftest update-status-long-test
  (let [url "https://api.twitter.com/1.1/statuses/update.json"
        user-params {:status (crypto.random/url-part 100)}
        unsigned-oauth (oauth/create-oauth-parameters config)
        signature (oauth/calculate-signature config
                                             (->> unsigned-oauth
                                                  (merge user-params)
                                                  oauth/generate-parameter-string
                                                  (oauth/generate-base-string url :post)))
        signed-oauth (assoc unsigned-oauth :oauth_signature signature)]
    (is (= 200
           (:status @(http/post url {:query-params (merge signed-oauth user-params)}))))))

(deftest update-status-test
  (is (= 200
         (:status @(twitter/update-status config (crypto.random/url-part 120))))))
