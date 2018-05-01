(ns twitter.oauth
  (:require [crypto.random]
            [clojure.string :as str])
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec)
           (org.apache.commons.codec.binary Base64)
           (org.apache.commons.codec.net URLCodec)))

(def signature-method "HMAC-SHA1")
(def version "1.0")

(defn hmac
  "Calculate HMAC signature for given data. Source: https://gist.github.com/jhickner/2382543#file-1-clj"
  [^String key ^String data]
  (let [hmac-sha1 "HmacSHA1"
        signing-key (SecretKeySpec. (.getBytes key) hmac-sha1)
        mac (doto (Mac/getInstance hmac-sha1) (.init signing-key))]
    (String. (Base64/encodeBase64
               (.doFinal mac (.getBytes data)))
             "UTF-8")))

(defn nonce []
  (crypto.random/url-part 22))

(defn timestamp []
  (-> (System/currentTimeMillis)
      (/ 1000)
      int))

(defn create-oauth-parameters
  [config]
  (let [{:keys [consumer-key token]} config]
    {:oauth_consumer_key     consumer-key
     :oauth_nonce            (nonce)
     :oauth_signature_method signature-method
     :oauth_timestamp        (timestamp)
     :oauth_token            token
     :oauth_version          version}))

(defn percent-encode
  [^String s]
  (->> s
       .getBytes
       (URLCodec/encodeUrl nil)
       String.))

(defn generate-parameter-string
  [parameters]
  (let [separator "&"
        sorted-parameters (sort parameters)]
    (str/join
      separator
      (map (fn [[k v]]
             (str (-> k name percent-encode) "=" (-> v str percent-encode)))
           sorted-parameters))))

(defn generate-base-string
  [url method parameter-string]
  (let [method (-> method name str/upper-case)
        encoded-url (percent-encode url)
        encoded-parameter-string (percent-encode parameter-string)]
    (str/join "&" [method encoded-url encoded-parameter-string])))

(defn calculate-signature
  [config signature-base-string]
  (let [{:keys [consumer-secret token-secret]} config
        key (str (percent-encode consumer-secret) "&" (percent-encode token-secret))]
    (hmac key signature-base-string)))
