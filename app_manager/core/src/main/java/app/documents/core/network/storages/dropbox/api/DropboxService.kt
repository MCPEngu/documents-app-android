package app.documents.core.network.storages.dropbox.api

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.storages.dropbox.models.explorer.DropboxItem
import app.documents.core.network.storages.dropbox.models.operations.MoveCopyBatchCheck
import app.documents.core.network.storages.dropbox.models.request.*
import app.documents.core.network.storages.dropbox.models.response.*
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface DropboxService {

    companion object {
        const val API_VERSION = "2/"
    }

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/list_folder")
    fun getFiles(@Body request: ExplorerRequest): Single<Response<ExplorerResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/list_folder/continue")
    fun getNextFileList(@Body request: ExplorerContinueRequest): Single<Response<ExplorerResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/delete_v2")
    fun delete(@Body request: PathRequest): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/create_folder_v2")
    fun createFolder(@Body request: CreateFolderRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/get_temporary_link")
    fun getExternalLink(@Body request: PathRequest): Single<Response<ExternalLinkResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/move_v2")
    fun move(@Body request: MoveRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/move_batch_v2")
    fun moveBatch(@Body request: MoveCopyBatchRequest): Single<Response<MoveCopyBatchResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/copy_v2")
    fun copy(@Body request: MoveRequest): Single<Response<MetadataResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/copy_batch_v2")
    fun copyBatch(@Body request: MoveCopyBatchRequest): Single<Response<MoveCopyBatchResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/copy_batch/check_v2")
    fun copyBatchCheck(@Body request: MoveCopyBatchCheck): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/move_batch/check_v2")
    fun moveBatchCheck(@Body request: MoveCopyBatchCheck): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/search_v2")
    fun search(@Body request: SearchRequest): Single<Response<SearchResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/search/continue_v2")
    fun searchNextList(@Body request: ExplorerContinueRequest): Single<Response<SearchResponse>>
}