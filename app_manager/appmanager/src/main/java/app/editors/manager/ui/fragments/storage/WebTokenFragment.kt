package app.editors.manager.ui.fragments.storage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.documents.core.network.common.contracts.StorageContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentStorageWebBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.StorageUtils
import app.documents.core.network.common.models.Storage
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.NetworkUtils
import javax.inject.Inject

class WebTokenFragment : BaseAppFragment(), SwipeRefreshLayout.OnRefreshListener {

    private var url: String? = null
    private var storage: Storage? = null
    private var redirectUrl: String? = null
    private var isPageLoad = false
    private var viewBinding: FragmentStorageWebBinding? = null
    private var webView: WebView? = null

    @Inject
    lateinit var preferenceTool: PreferenceTool
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentStorageWebBinding.inflate(layoutInflater, container, false)        
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TAG_PAGE_LOAD, isPageLoad)

        // Save WebView state
        webView?.let {
            val bundle = Bundle()
            it.saveState(bundle)
            outState.putBundle(TAG_WEB_VIEW, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CookieManager.getInstance().removeAllCookies(null)
        viewBinding = null
    }

    override fun onRefresh() {
        loadWebView(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init(savedInstanceState: Bundle?) {
        viewBinding?.let {
            setActionBarTitle(getString(R.string.storage_web_title))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            isPageLoad = false
            it.webStorageSwipe.setOnRefreshListener(this)
            it.webStorageSwipe.setColorSchemeColors(ContextCompat
                .getColor(requireContext(),lib.toolkit.base.R.color.colorSecondary))
            webView = it.webStorageWebview.apply {
                settings.javaScriptEnabled = true
//                settings.setAppCacheEnabled(false)
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.userAgentString = getString(R.string.google_user_agent)
                webViewClient = WebViewCallbacks()
                clearHistory()
            }
            getArgs()
            restoreStates(savedInstanceState)
        }
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            storage = bundle.getParcelable(TAG_STORAGE)
            if (storage != null) {
                url = StorageUtils.getStorageUrl(
                    storage?.name,
                    storage?.clientId,
                    storage?.redirectUrl
                )
                redirectUrl = storage?.redirectUrl
            }
        }
    }

    private fun restoreStates(savedInstanceState: Bundle?) {
        savedInstanceState?.let { state ->
            if (state.containsKey(TAG_WEB_VIEW)) {
                viewBinding?.webStorageSwipe?.isRefreshing = true
                state.getBundle(TAG_WEB_VIEW)?.let {
                    webView?.restoreState(it)
                }
            }
            if (state.containsKey(TAG_PAGE_LOAD)) {
                isPageLoad = state.getBoolean(TAG_PAGE_LOAD)
            }
        } ?: run {
            loadWebView(url)
        }
    }

    private fun loadWebView(url: String?) {
        viewBinding?.webStorageSwipe?.isRefreshing = true
        url?.let { webView?.loadUrl(it) }
    }

    /*
     * WebView callback class
     * Example token response:
     *       https://service.teamlab.info/oauth2.aspx?code=4/AAAJYg3drTzabIIAPiYq_FEieoyhj7FqOjON8k0l3kEN3v5Qc3xmA_Hqp3TxSa5aiwSSToMJefTDDZcrJJLfguQ#
     *       https://login.live.com/err.srf?lc=1049#error=invalid_request&error_description=The+provided+value+for+the+input+parameter+'redirect_uri'+is+not+valid.+The+expected+value+is+'https://login.live.com/oauth20_desktop.srf'+or+a+URL+which+matches+the+redirect+URI+registered+for+this+client+application.
     *       https://login.live.com/oauth20_authorize.srf?client_id=000000004413039F&redirect_uri=https://service.teamlab.info/oauth2.aspx&response_type=code&scope=wl.signin%20wl.skydrive_update%20wl.offline_access
     * */
    private inner class WebViewCallbacks : WebViewClient() {
        
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            if (url.startsWith(redirectUrl!!)) {
                val uri = Uri.parse(url)
                val token = uri.getQueryParameter(StorageContract.ARG_CODE)
                if (token?.equals("null", ignoreCase = true) == false) {
                    showFragment(
                        ConnectFragment.newInstance(token, storage),
                        ConnectFragment.TAG,
                        false
                    )
                }
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            viewBinding?.webStorageSwipe?.isRefreshing = false
            isPageLoad = true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            viewBinding?.webStorageSwipe?.isRefreshing = false
            if (!NetworkUtils.isOnline(requireContext())) {
                showSnackBar(R.string.errors_connection_error)
            }
        }
    }

    companion object {
        val TAG: String = WebTokenFragment::class.java.simpleName
        private const val TAG_STORAGE = "TAG_MEDIA"
        private const val TAG_WEB_VIEW = "TAG_WEB_VIEW"
        private const val TAG_PAGE_LOAD = "TAG_PAGE_LOAD"
        
        fun newInstance(storage: Storage?): WebTokenFragment =
            WebTokenFragment().apply { 
                arguments = Bundle(1).apply {
                    putParcelable(TAG_STORAGE, storage)
                }
            }
    }
}