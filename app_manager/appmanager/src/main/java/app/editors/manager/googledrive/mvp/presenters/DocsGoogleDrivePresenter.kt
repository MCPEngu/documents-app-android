package app.editors.manager.googledrive.mvp.presenters

import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.googledrive.managers.providers.GoogleDriveFileProvider
import app.editors.manager.googledrive.mvp.views.DocsGoogleDriveView
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.onedrive.managers.providers.OneDriveFileProvider
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

class DocsGoogleDrivePresenter: DocsBasePresenter<DocsGoogleDriveView>() {

    init {
        App.getApp().appComponent.inject(this)
        mModelExplorerStack = ModelExplorerStack()
        mFilteringValue = ""
        mPlaceholderType = PlaceholderViews.Type.NONE
        mIsContextClick = false
        mIsFilteringMode = false
        mIsSelectionMode = false
        mIsFoldersMode = false
    }


    fun getProvider() {
        mFileProvider?.let {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                    withContext(Dispatchers.Main) {
                        getItemsById(null)
                    }

                }
            }
        } ?: run {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
                    AccountUtils.getAccount(mContext, cloudAccount.getAccountName())?.let {
                        mFileProvider = GoogleDriveFileProvider()
                        withContext(Dispatchers.Main) {
                            getItemsById(null)
                        }
                    }
                } ?: run {
                    throw Error("Not accounts")
                }
            }
        }
    }

    override fun getNextList() {
        TODO("Not yet implemented")
    }

    override fun createDocs(title: String) {
        TODO("Not yet implemented")
    }

    override fun getFileInfo() {
        TODO("Not yet implemented")
    }

    override fun addRecent(file: CloudFile?) {
        TODO("Not yet implemented")
    }

    override fun updateViewsState() {
        if (mIsSelectionMode) {
            viewState.onStateUpdateSelection(true)
            viewState.onActionBarTitle(mModelExplorerStack.countSelectedItems.toString())
            viewState.onStateAdapterRoot(mModelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (mIsFilteringMode) {
            viewState.onActionBarTitle(mContext.getString(R.string.toolbar_menu_search_result))
            viewState.onStateUpdateFilter(true, mFilteringValue)
            viewState.onStateAdapterRoot(mModelExplorerStack.isNavigationRoot)
            viewState.onStateActionButton(false)
        } else if (!mModelExplorerStack.isRoot) {
            viewState.onStateAdapterRoot(false)
            viewState.onStateUpdateRoot(false)
            viewState.onStateActionButton(true)
            viewState.onActionBarTitle(if(currentTitle.isEmpty()) { mItemClicked?.title } else { currentTitle } )
        } else {
            if (mIsFoldersMode) {
                viewState.onActionBarTitle(mContext.getString(R.string.operation_title))
                viewState.onStateActionButton(false)
            } else {
                viewState.onActionBarTitle("")
                viewState.onStateActionButton(true)
            }
            viewState.onStateAdapterRoot(true)
            viewState.onStateUpdateRoot(true)
        }
    }

    override fun delete(): Boolean {
        if (mModelExplorerStack.countSelectedItems > 0) {
            viewState.onDialogQuestion(
                mContext.getString(R.string.dialogs_question_delete), null,
                TAG_DIALOG_BATCH_DELETE_SELECTED
            )
        } else {
            deleteItems()
        }
        return true
    }

    override fun onContextClick(item: Item?, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        mIsContextClick = true
        val state = ContextBottomDialog.State()
        state.title = itemClickedTitle
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = !isClickedItemFile
        state.isDocs = isClickedItemDocs
        state.isWebDav = false
        state.isOneDrive = false
        state.isDropBox = false
        state.isGoogleDrive = true
        state.isTrash = isTrash
        state.isItemEditable = true
        state.isContextEditable = true
        state.isCanShare = true
        if (!isClickedItemFile) {
            state.iconResId = R.drawable.ic_type_folder
        } else {
            state.iconResId = getIconContext(
                StringUtils.getExtensionFromPath(
                    itemClickedTitle
                )
            )
        }
        state.isPdf = isPdf
        if (state.isShared && state.isFolder) {
            state.iconResId = R.drawable.ic_type_folder_shared
        }
        viewState.onItemContext(state)
    }

    override fun onActionClick() {
        TODO("Not yet implemented")
    }
}