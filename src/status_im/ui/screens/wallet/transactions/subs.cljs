(ns status-im.ui.screens.wallet.transactions.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.money :as money]
            [status-im.utils.transactions :as transactions]
            [status-im.utils.ethereum.core :as ethereum]))

(reg-sub :wallet.transactions/transactions-loading?
         :<- [:wallet]
         (fn [wallet]
           (:transactions-loading? wallet)))

(reg-sub :wallet.transactions/current-tab
         :<- [:wallet]
         (fn [wallet]
           (get wallet :current-tab 0)))

(defn enrich-transaction [{:keys [type to from timestamp] :as transaction} contacts]
  (let [[contact-address key-contact key-wallet] (if (= type :inbound)
                                                   [from :from-contact :to-wallet]
                                                   [to :to-contact :from-wallet])
        wallet                                   (i18n/label :main-wallet)
        contact                                  (get contacts (utils.hex/normalize-hex contact-address))]
    (cond-> transaction
      contact (assoc key-contact (:name contact))
      :always (assoc key-wallet wallet
                     :time-formatted (datetime/timestamp->time timestamp)))))

(reg-sub :wallet.transactions/transactions
         :<- [:wallet]
         :<- [:get-contacts-by-address]
         (fn [[wallet contacts]]
           (reduce (fn [acc [hash transaction]]
                     (assoc acc hash (enrich-transaction transaction contacts)))
                   {}
                   (:transactions wallet))))

(reg-sub :wallet.transactions/grouped-transactions
         :<- [:wallet.transactions/transactions]
         (fn [transactions]
           (group-by :type (vals transactions))))

(reg-sub :wallet.transactions/pending-transactions-list
         :<- [:wallet.transactions/grouped-transactions]
         (fn [{:keys [pending]}]
           (when pending
             {:title "Pending"
              :key   :pending
              :data  pending})))

(reg-sub :wallet.transactions/failed-transactions-list
         :<- [:wallet.transactions/grouped-transactions]
         (fn [{:keys [failed]}]
           (when failed
             {:title "Failed"
              :key   :failed
              :data  failed})))

(defn group-transactions-by-date [transactions]
  (->> transactions
       (group-by #(datetime/timestamp->date-key (:timestamp %)))
       (sort-by key)
       reverse
       (map (fn [[date-key transactions]]
              {:title (datetime/timestamp->mini-date (:timestamp (first transactions)))
               :key   date-key
               :data  (sort-by :timestamp > transactions)}))))

(reg-sub :wallet.transactions/completed-transactions-list
         :<- [:wallet.transactions/grouped-transactions]
         (fn [{:keys [inbound outbound failed]}]
           (group-transactions-by-date (concat inbound outbound failed))))

(reg-sub :wallet.transactions/transactions-history-list
         :<- [:wallet.transactions/pending-transactions-list]
         :<- [:wallet.transactions/completed-transactions-list]
         (fn [[pending completed]]
           (cond-> []
             pending   (into pending)
             completed (into completed))))

(reg-sub :wallet.transactions/current-transaction
         :<- [:wallet]
         (fn [wallet]
           (:current-transaction wallet)))

(defn- value->fiat [value symbol {:keys [code]} prices precision]
  (let [fiat-price (get-in prices [symbol (keyword code) :price])
        value-ether (money/wei->ether value)
        value-fiat (money/crypto->fiat value-ether fiat-price)]
    (-> value-fiat (money/with-precision precision) (money/to-fixed))))

(defn- cost-fiat [gas-used gas-price symbol currency prices]
  (let [value (money/fee-value gas-used gas-price)
        amount (value->fiat value symbol currency prices 4)]
    (str amount " " (:code currency))))

(reg-sub :wallet.transactions/transaction-details
         :<- [:wallet.transactions/transactions]
         :<- [:wallet.transactions/current-transaction]
         :<- [:network]
         :<- [:wallet/currency]
         :<- [:prices]
         (fn [[transactions current-transaction network currency prices]]
           (let [{:keys [gas-used gas-price hash symbol timestamp type value] :as transaction} (get transactions current-transaction)
                 chain (ethereum/network->chain-keyword network)]
             (when transaction
               (merge transaction
                      {:date           (datetime/timestamp->long-date timestamp)
                       :gas-price-eth  (if gas-price (money/wei->str :eth gas-price) "-")
                       :gas-price-gwei (if gas-price (money/wei->str :gwei gas-price) "-")
                       :symbol-fiat    (:id currency)
                       :value-fiat     (value->fiat value symbol currency prices 2)}
                      (if (= type :unsigned)
                        {:block     (i18n/label :not-applicable)
                         :cost      (i18n/label :not-applicable)
                         :cost-fiat (i18n/label :not-applicable)
                         :gas-limit (i18n/label :not-applicable)
                         :gas-used  (i18n/label :not-applicable)
                         :nonce     (i18n/label :not-applicable)
                         :hash      (i18n/label :not-applicable)}
                        {:cost      (when gas-used
                                      (money/wei->str :eth (money/fee-value gas-used gas-price)))
                         :cost-fiat (cost-fiat gas-used gas-price symbol currency prices)
                         :url       (transactions/get-transaction-details-url chain hash)}))))))

(reg-sub :wallet.transactions.details/confirmations
         :<- [:wallet.transactions/transaction-details]
         (fn [transaction-details]
    ;;TODO (yenda) this field should be calculated based on the current-block and the block of the transaction
           (:confirmations transaction-details)))

(reg-sub :wallet.transactions.details/confirmations-progress
         :<- [:wallet.transactions.details/confirmations]
         (fn [confirmations]
           (let [max-confirmations 10]
             (if (>= confirmations max-confirmations)
               100
               (* 100 (/ confirmations max-confirmations))))))

(reg-sub :wallet.transactions/filters
         (fn [db]
           (get-in db [:wallet.transactions :filters])))

(reg-sub :wallet.transactions/error-message?
         :<- [:wallet]
         (fn [wallet]
           (get-in wallet [:errors :transactions-update])))
