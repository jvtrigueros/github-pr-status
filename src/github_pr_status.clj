(ns github-pr-status
  (:require [github.core :as github]
            [twitter.core :as twitter]
            [clojure.string :as str]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn get-all-pull-requests
  "Collects all the pull requests from a repo. If the config contains an :id only
  the pull requests up to that id will be collected."
  [config]
  (let [token (:token config)
        variables (github/config->variables config)]
    (if-let [id (:id config)]
      (->> id
           (github/get-latest-pull-requests token variables)
           rseq)
      (github/get-pull-requests token variables))))

(defn format-pull-request
  "Formats a pull request in a tweetable format:

  <PR#>:<Title>
  <Body>
  <url>

  Truncates body so that the tweet character limit isn't reached."
  [pull-request]
  (let [{:keys [number title body url]} pull-request
        clean-body (str/replace body #"\r" "")
        combined-title (str number ": " title "\n")
        ellipsis "...\n"]
    (str combined-title
         (subs clean-body 0 (min (count clean-body)
                                 (- twitter/max-tweet-length
                                    (count combined-title)
                                    (count ellipsis)
                                    (count url))))
         ellipsis
         url)))

(defn tweet-pull-requests!
  "Formats and tweets a collection of pull requests."
  [config pull-requests]
  (doseq [pull-request pull-requests]
    (timbre/info "Tweeting:" (:title pull-request) (subs (:body pull-request) 0 10))
    (timbre/info "Response Code" (:status @(twitter/update-status config
                                                                (format-pull-request pull-request))))))

(defn -main [& args]
  (if-let [config-path (first args)]
    (let [config (read-string (slurp config-path))
          pull-requests (get-all-pull-requests (:github config))]
      (timbre/info "Collected" (count pull-requests) "pull requests")
      (tweet-pull-requests! (:twitter config) pull-requests)
      (if (not-empty pull-requests)
        (->> pull-requests
             last
             :id
             (assoc-in config [:github :id])
             (spit config-path))))
    (println "missing config file path")))
