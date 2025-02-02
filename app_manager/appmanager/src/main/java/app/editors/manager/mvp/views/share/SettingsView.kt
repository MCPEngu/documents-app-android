package app.editors.manager.mvp.views.share

import android.content.Intent
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.mvp.views.base.BaseViewExt
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface SettingsView : BaseViewExt {
    fun onGetShare(list: List<ViewType>, accessCode: Int)
    fun onRemove(share: ShareUi, sharePosition: Int)
    fun onGetShareItem(entity: ViewType, sharePosition: Int, accessCode: Int)
    fun onExternalAccess(accessCode: Int, isMessage: Boolean)
    fun onInternalLink(internalLink: String)
    fun onItemType(isFolder: Boolean)
    fun onAddShare(item: Item)
    fun onPlaceholderState(type: PlaceholderViews.Type)
    fun onActionButtonState(isVisible: Boolean)
    fun onResultState(isShared: Boolean)
    fun onSendLink(intent: Intent)
    fun onButtonState(isVisible: Boolean)
    fun onPopupState(state: Boolean)
    fun onShowPopup(sharePosition: Int, isVisitor: Boolean)
    fun onUpdateAvatar(share: ShareUi)
}