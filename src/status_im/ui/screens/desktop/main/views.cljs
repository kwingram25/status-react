(ns status-im.ui.screens.desktop.main.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.desktop.main.tabs.profile.views :as profile.views]
            [status-im.ui.screens.desktop.main.tabs.home.views :as home.views]
            [status-im.ui.screens.desktop.main.chat.views :as chat.views]
            [status-im.ui.screens.desktop.main.add-new.views :as add-new.views]
            [status-im.ui.components.desktop.tabs :as tabs]
            [status-im.ui.components.react :as react]))

(views/defview status-view []
  [react/view {:style {:flex 1 :background-color "#eef2f5" :align-items :center :justify-content :center}}
   [react/text {:style {:font-size 18 :color "#939ba1"}}
    "Status.im"]])

(views/defview tab-views []
  (views/letsubs [tab [:get-in [:desktop/desktop :tab-view-id]]]
    (let [component (case tab
                      :profile profile.views/profile
                      :home home.views/chat-list-view
                      react/view)]
      [react/view {:style {:flex 1}}
       [component]])))

(views/defview main-view []
  (views/letsubs [view-id [:get :view-id]]
    (let [component (case view-id
                      :chat        chat.views/chat-view
                      :new-contact add-new.views/new-contact
                      :qr-code     profile.views/qr-code
                      status-view)]
      [react/view {:style {:flex 1}}
       [component]])))

(views/defview main-views []
  [react/view {:style {:flex 1 :flex-direction :row}}
   [react/view {:style {:width 340 :background-color :white}}
    [react/view {:style {:flex 1}}
     [tab-views]]
    [tabs/main-tabs]]
   [react/view {:style {:width 1 :background-color "#e8ebec"}}]
   [main-view]])
