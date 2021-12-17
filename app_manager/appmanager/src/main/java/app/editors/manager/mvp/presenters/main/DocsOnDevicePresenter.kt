package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.content.ClipData
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.work.Data
import app.documents.core.account.Recent
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.webDavApi
import app.editors.manager.managers.providers.LocalFileProvider
import app.editors.manager.managers.providers.ProviderError
import app.editors.manager.managers.providers.WebDavFileProvider
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.views.main.DocsOnDeviceView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.*
import moxy.InjectViewState
import java.io.File
import java.util.*

@InjectViewState
class DocsOnDevicePresenter : DocsBasePresenter<DocsOnDeviceView>() {

    companion object {
        val TAG: String = DocsOnDevicePresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
        mModelExplorerStack = ModelExplorerStack()
        mFilteringValue = ""
        mPlaceholderType = PlaceholderViews.Type.NONE
        mIsContextClick = false
        mIsFilteringMode = false
        mIsSelectionMode = false
        mIsFoldersMode = false
        mFileProvider = LocalFileProvider(LocalContentTools(mContext))
        checkWebDav()
    }

    private var mPhotoUri: Uri? = null
    private var webDavFileProvider: WebDavFileProvider? = null

    private fun checkWebDav() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                if (it.isWebDav) {
                    webDavFileProvider = WebDavFileProvider(
                        mContext.webDavApi(),
                        WebDavApi.Providers.valueOf(it.webDavProvider ?: "")
                    )
                }
            }
        }
    }

    override fun getNextList() {
        // Stub to local
    }

    override fun createDocs(title: String) {
        val id = mModelExplorerStack.currentId
        if (id != null) {
            val requestCreate = RequestCreate()
            requestCreate.title = title
            mDisposable.add(mFileProvider.createFile(id, requestCreate)
                .subscribe({ file: CloudFile ->
                    addFile(file)
                    addRecent(file)
                    openFile(file)
                }) { viewState.onError(mContext.getString(R.string.errors_create_local_file)) })
        }
    }

    override fun getFileInfo() {
        if (mItemClicked != null && mItemClicked is CloudFile) {
            val file = mItemClicked as CloudFile
            addRecent(file)
            openFile(file)
        }
    }

    override fun addRecent(file: CloudFile) {
        CoroutineScope(Dispatchers.Default).launch {
            recentDao.addRecent(
                Recent(
                    idFile = null,
                    path = file.webUrl,
                    name = file.title,
                    size = file.pureContentLength,
                    isLocal = true,
                    isWebDav = false,
                    date = Date().time
                )
            )
        }

    }

    private fun addRecent(uri: Uri) {
        CoroutineScope(Dispatchers.Default).launch {
            DocumentFile.fromSingleUri(mContext, uri)?.let { file ->
                recentDao.addRecent(
                    Recent(
                        idFile = null,
                        path = uri.toString(),
                        name = file.name ?: "",
                        size = file.length(),
                        isLocal = true,
                        isWebDav = false,
                        date = Date().time,
                    )
                )
            }
        }
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
            viewState.onActionBarTitle(currentTitle)
        } else {
            if (mIsFoldersMode) {
                viewState.onActionBarTitle(mContext.getString(R.string.operation_title))
                viewState.onStateActionButton(false)
            } else {
                viewState.onActionBarTitle(mContext.getString(R.string.fragment_on_device_title))
                viewState.onStateActionButton(true)
            }
            viewState.onStateAdapterRoot(true)
            viewState.onStateUpdateRoot(true)
        }
    }

    override fun onContextClick(item: Item, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        mIsContextClick = true
        val state = ContextBottomDialog.State()
        state.isLocal = true
        state.title = item.title
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = item is CloudFolder
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
        viewState.onActionDialog()
    }

    override fun deleteItems() {
        val items: MutableList<Item> = ArrayList()
        val files = mModelExplorerStack.selectedFiles
        val folders = mModelExplorerStack.selectedFolders
        items.addAll(folders)
        items.addAll(files)
        mDisposable.add(mFileProvider.delete(items, null)
            .subscribe({ }, { fetchError(it) }) {
                mModelExplorerStack.removeSelected()
                backStack
                setPlaceholderType(if (mModelExplorerStack.isListEmpty) PlaceholderViews.Type.EMPTY else PlaceholderViews.Type.NONE)
                viewState.onRemoveItems(items)
                viewState.onSnackBar(mContext.getString(R.string.operation_complete_message))
            })
    }

    override fun uploadToMy(uri: Uri) {
        mContext.accountOnline?.let { account ->
            if (webDavFileProvider == null) {
                when {
                    mPreferenceTool.uploadWifiState && !NetworkUtils.isWifiEnable(mContext) -> {
                        viewState.onSnackBar(mContext.getString(R.string.upload_error_wifi))
                    }
                    ContentResolverUtils.getSize(mContext, uri) > FileUtils.STRICT_SIZE -> {
                        viewState.onSnackBar(mContext.getString(R.string.upload_manager_error_file_size))
                    }
                    else -> {
                        if (!account.isWebDav) {
                            val workData = Data.Builder()
                                .putString(UploadWork.TAG_UPLOAD_FILES, uri.toString())
                                .putString(UploadWork.ACTION_UPLOAD_MY, UploadWork.ACTION_UPLOAD_MY)
                                .putString(UploadWork.TAG_FOLDER_ID, null)
                                .build()
                            startUpload(workData)
                        }
                    }
                }
            } else {
                uploadWebDav(account.webDavPath ?: "", listOf(uri))
            }
        }
    }

    private fun uploadWebDav(id: String, uriList: List<Uri>) {
        var id = id
        if (id[id.length - 1] != '/') {
            id = "$id/"
        }
        mUploadDisposable = webDavFileProvider!!.upload(id, uriList)!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }, { throwable: Throwable -> fetchError(throwable) }
            ) {
                viewState.onDialogClose()
                viewState.onSnackBar(mContext.getString(R.string.upload_manager_complete))
                for (file in (mFileProvider as WebDavFileProvider).uploadsFile) {
                    addFile(file)
                }
                (mFileProvider as WebDavFileProvider).uploadsFile.clear()
            }
    }

    override fun sortBy(value: String, isRepeatedTap: Boolean): Boolean {
        mPreferenceTool.sortBy = value
        if (isRepeatedTap) {
            reverseSortOrder()
        }
        getItemsById(mModelExplorerStack.currentId)
        return true
    }

    override fun orderBy(value: String): Boolean {
        mPreferenceTool.sortOrder = value
        getItemsById(mModelExplorerStack.currentId)
        return true
    }

    override fun rename(title: String?) {
        val item = mModelExplorerStack.getItemById(mItemClicked)
        if (item != null) {
            val existFile = File(item.id)
            if (existFile.exists()) {
                val path = StringBuilder()
                path.append(existFile.parent).append("/").append(title)
                if (item is CloudFile) {
                    path.append(item.fileExst)
                }
                val renameFile = File(path.toString())
                if (renameFile.exists()) {
                    viewState.onError(mContext.getString(R.string.rename_file_exist))
                } else {
                    super.rename(title)
                }
            }
        }
    }

    fun moveFile(data: Uri?, isCopy: Boolean) {
        val path = PathUtils.getFolderPath(mContext, data!!)
        if (isSelectionMode) {
            moveSelection(path, isCopy)
            return
        }
        try {
            if ((mFileProvider as LocalFileProvider).transfer(path, mItemClicked, isCopy)) {
                refresh()
                viewState.onSnackBar(mContext.getString(R.string.operation_complete_message))
            } else {
                viewState.onError(mContext.getString(R.string.operation_error_move_to_same))
            }
        } catch (e: Exception) {
            catchTransferError(e)
        }
    }

    fun openFromChooser(uri: Uri) {
        val fileName = ContentResolverUtils.getName(mContext, uri)
        val ext = StringUtils.getExtensionFromPath(fileName.lowercase())

        mDisposable.add((mFileProvider as LocalFileProvider).import(mContext, mModelExplorerStack.currentId!!, uri).subscribe {
            refresh()
            viewState.onSnackBar(mContext.getString(R.string.operation_complete_message))
            addRecent(uri)
            openFile(uri, ext)
        })
    }

    private fun openFile(file: CloudFile) {
        val path = file.id
        val uri = Uri.fromFile(File(path))
        val ext = StringUtils.getExtensionFromPath(file.id.lowercase())
        openFile(uri, ext)
    }

    private fun openFile(uri: Uri, ext: String) {
        when (StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC, StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.FORM -> viewState.onShowDocs(uri)
            StringUtils.Extension.SHEET -> viewState.onShowCells(uri)
            StringUtils.Extension.PRESENTATION -> viewState.onShowSlides(uri)
            StringUtils.Extension.PDF -> viewState.onShowPdf(uri)
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> showMedia(uri)
            else -> viewState.onError(mContext.getString(R.string.error_unsupported_format))
        }
    }

    private fun moveSelection(path: String?, isCopy: Boolean) {
        if (mModelExplorerStack.countSelectedItems > 0) {
            if (mFileProvider is LocalFileProvider) {
                val provider = mFileProvider as LocalFileProvider
                val items: MutableList<Item> = ArrayList()
                val files = mModelExplorerStack.selectedFiles
                val folders = mModelExplorerStack.selectedFolders
                items.addAll(folders)
                items.addAll(files)
                for (item in items) {
                    try {
                        if (!provider.transfer(path, item, isCopy)) {
                            viewState.onError(mContext.getString(R.string.operation_error_move_to_same))
                            break
                        }
                    } catch (e: Exception) {
                        catchTransferError(e)
                    }
                }
                backStack
                refresh()
                viewState.onSnackBar(mContext.getString(R.string.operation_complete_message))
            }
        } else {
            viewState.onError(mContext.getString(R.string.operation_empty_lists_data))
        }
    }

    fun showDeleteDialog() {
        if (mItemClicked != null) {
            if (mItemClicked is CloudFolder) {
                viewState.onDialogQuestion(
                    mContext.getString(R.string.dialogs_question_delete),
                    mContext.getString(R.string.dialog_question_delete_folder),
                    TAG_DIALOG_DELETE_CONTEXT
                )
            } else {
                viewState.onDialogQuestion(
                    mContext.getString(R.string.dialogs_question_delete),
                    mContext.getString(R.string.dialog_question_delete_file),
                    TAG_DIALOG_DELETE_CONTEXT
                )
            }
        }
    }

    fun deleteFile() {
        if (mItemClicked != null) {
            val items: MutableList<Item> = ArrayList()
            items.add(mItemClicked!!)
            mDisposable.add(mFileProvider.delete(items, null)
                .subscribe({ }, { }) {
                    mModelExplorerStack.removeItemById(mItemClicked!!.id)
                    viewState.onRemoveItem(mItemClicked)
                    viewState.onSnackBar(mContext.getString(R.string.operation_complete_message))
                })
        }
    }

    @SuppressLint("MissingPermission")
    fun createPhoto() {
        val photo = FileUtils.createFile(File(stack.current.id), TimeUtils.fileTimeStamp, "png")
        if (photo != null) {
            mPhotoUri = ContentResolverUtils.getFileUri(mContext, photo)
            viewState.onShowCamera(mPhotoUri)
        }
    }

    fun deletePhoto() {
        if (mPhotoUri != null) {
            mContext.contentResolver.delete(mPhotoUri!!, null, null)
        }
    }

    fun checkSelectedFiles() {
        if (mModelExplorerStack.countSelectedItems > 0) {
            viewState.onShowFolderChooser()
        } else {
            viewState.onError(mContext.getString(R.string.operation_empty_lists_data))
        }
    }

    fun upload() {
        mItemClicked?.let { item ->
            mContext.accountOnline?.let {
                Uri.fromFile(File(item.id))?.let { uri ->
                    uploadToMy(uri)
                }
            } ?: run {
                viewState.onShowPortals()
            }
        }

    }

    @SuppressLint("MissingPermission")
    fun showMedia(uri: Uri) {
        viewState.onOpenMedia(OpenState.Media(getMediaFile(uri), false))
    }

    private fun getMediaFile(uri: Uri): Explorer =
        Explorer().apply {
            val file = File(PathUtils.getPath(mContext, uri).toString())
            val explorerFile = CloudFile().apply {
                pureContentLength = file.length()
                webUrl = file.absolutePath
                fileExst = StringUtils.getExtensionFromPath(file.name)
                title = file.name
                isClicked = true
            }
            current = Current().apply {
                title = file.name
                filesCount = "1"
            }
            files = listOf(explorerFile)
            addRecent(explorerFile)
        }

    override fun fetchError(throwable: Throwable) {
        if (throwable.message != null) {
            if (throwable.message == ProviderError.ERROR_CREATE_LOCAL) {
                viewState.onError(mContext.getString(R.string.rename_file_exist))
            } else {
                super.fetchError(throwable)
            }
        }
    }

    private fun catchTransferError(e: Exception) {
        if (e.message != null) {
            when (e.message) {
                ProviderError.FILE_EXIST -> viewState.onError(mContext.getString(R.string.operation_error_move_to_same))
                ProviderError.UNSUPPORTED_PATH -> viewState.onError(mContext.getString(R.string.error_unsupported_path))
            }
        } else {
            Log.e(TAG, "Error move/copy local")
        }
    }

    fun updateState() {
        setSelection(false)
        setFiltering(false)
        updateViewsState()
    }
}