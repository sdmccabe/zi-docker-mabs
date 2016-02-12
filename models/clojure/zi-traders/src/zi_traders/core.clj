;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Zero Intelligence Traders
;; Version 5
;; CSS 739 - Advanced Agent-Based Modeling
;; George Mason University
;; Fall 2015 - 25 October 2015
;; Author: Dale K. Brearcliffe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(ns zi-traders.core
  (:gen-class)
  (require [clojure.tools.cli :as c]))

(defn lower-buyer-bound [segment-num agents-per-thread]
   "Calculates the lower vector boundary for a buyer"
   (* segment-num agents-per-thread))

(defn upper-buyer-bound [segment-num agents-per-thread]
   "Calculates the upeer vector boundary for a buyer"
   (- (* (+ segment-num 1) agents-per-thread) 1))

(defn lower-seller-bound [segment-num agents-per-thread]
   "Calculates the lower vector boundary for a seller"
   (* segment-num agents-per-thread))

(defn upper-seller-bound [segment-num agents-per-thread]
   "Calculates the upper vector boundary for a seller"
   (- (* (+ segment-num 1) agents-per-thread) 1))

(defn update-agent [agents idx dtk vl]
   "Given a vector of buyers or sellers, atomically change a value 
   of one of the agents"
   (swap! agents assoc idx (assoc (nth @agents idx) dtk vl)))

(defn initialize [num-threads num-agents num-trades]
   ; Some general parameters for the simulation 
   (def max-number-of-trades num-trades)
   (def max-buyer-value 30)
   (def max-seller-value 30)
   (def number-of-buyers num-agents)
   (def number-of-sellers num-agents)
   (def agents-per-thread (quot number-of-buyers num-threads))
   (def trades-per-thread (quot max-number-of-trades num-threads))
   ; Some 'constants' for the agent to make the program easier to read
   (def agent-name 0)
   (def agent-quantity 1)
   (def agent-value 2)
   (def agent-price 3)
   ; Create the buyer and seller atomic vectors
   (def buyers (atom (mapv (fn [[el]] [el 0 (+ (rand-int max-buyer-value) 1) 0])
            (vec (map vec (partition 1 (map #(str "Buyer-" %) (range number-of-buyers))))))))
   (def sellers (atom (mapv (fn [[el]] [el 1 (+ (rand-int max-seller-value) 1) 0])
            (vec (map vec (partition 1 (map #(str "Seller-" %) (range number-of-sellers))))))))) ; END initialize

(defn do-trades [segment-num]
   ; Check for trades with a section of the buyer/seller vector delimited by 'segment-num'
   (loop [i 1]
      (when (<= i trades-per-thread)
         (let [
            ; Pick a buyer at random, then pick a 'bid' price randomly between 1 and the agent's private value
            buyer-index (+ (lower-buyer-bound segment-num agents-per-thread)
                           (rand-int (+ (- (upper-buyer-bound segment-num agents-per-thread) (lower-buyer-bound segment-num agents-per-thread)) 1)))
            bid-price (+ (rand-int (nth (nth @buyers buyer-index) agent-value)) 1) 
            ;Pick a seller at random, then pick an 'ask' price randomly between the agent's private value and maxSellerValue
            seller-index (+ (lower-seller-bound segment-num agents-per-thread)
                            (rand-int (+ (- (upper-seller-bound segment-num agents-per-thread) (lower-seller-bound segment-num agents-per-thread)) 1)))
            ask-price (+ (rand-int (- max-seller-value (+ (nth (nth @sellers seller-index) agent-value) 1))) 
                         (nth (nth @sellers seller-index) agent-value))
            ; First, compute the transaction price...
            transaction-price (+ ask-price (rand-int (+ (- bid-price ask-price) 1)))
            ]
            ; Let's see if a deal can be made...
            (when (and (= (nth (nth @buyers buyer-index) agent-quantity) 0) (and (= (nth (nth @sellers seller-index) agent-quantity) 1) (>= bid-price ask-price)))              
               ; Save the transaction price...
               (update-agent buyers buyer-index agent-price transaction-price)
               (update-agent sellers seller-index agent-price transaction-price)        
               ; Then execute the exchange...
               (update-agent buyers buyer-index agent-quantity 1)
               (update-agent sellers seller-index agent-quantity 0)))
         (recur (inc i))))) ; END do-trades

(defn compute-statistics []
   "Calculate then display the results of the trades"
   (let [number-bought (reduce + (map (fn [[_ el]] el) @buyers))
      number-sold (- number-of-sellers (reduce + (map (fn [[_ el]] el) @sellers)))
      avg-price (double (/ (+ (reduce + (map (fn [[_ _ _ el]] el) @buyers)) 
                              (reduce + (map (fn [[_ _ _ el]] el) @sellers)))
                           (+ number-bought number-sold)))
      sd (Math/sqrt (double (/ (- (+ (reduce + (map (fn [[_ _ _ el]] (* el el)) @buyers)) 
                                     (reduce + (map (fn [[_ _ _ el]] (* el el)) @sellers))) 
                                  (* (+ number-bought number-sold) (* avg-price avg-price))) 
                               (- (+ number-bought number-sold) 1))))]
      
      (println (str number-bought " items bought and " number-sold " items sold"))         
      (println (str "The average price = " avg-price " and the s.d. is " sd "\\n")))) ; END compute-statistics

(defn start-trading [num-threads num-agents num-trades]
   
   (println (str "\\nZERO INTELLIGENCE TRADERS\\n"))
   
   (initialize num-threads num-agents num-trades)
   
   (time (let [threads (range num-threads)
         future-work (doall (map #(future (do-trades %)) threads))
         results (map deref future-work)]
         (doseq [s results] )))
   
   (compute-statistics)
   (shutdown-agents)) ; END start-trading

(defn -main [& args]
  (let [[options args banner]
       (c/cli args
              ["-a" "number of agents" :default 1000
                                       :parse-fn #(Integer. %)]
              ["-n" "maximum number of trades" :default 10000
                                       :parse-fn #(Integer. %)]
                ["-t" "threads to use" :default 1
                                       :parse-fn #(Integer. %)])]
  (start-trading (:t options) (:a options) (:n options))))