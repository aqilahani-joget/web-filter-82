package org.joget.joget82;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.PluginWebFilterAbstract;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.plugin.base.SystemConfigurablePlugin;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class SampleWebFilterPlugin extends PluginWebFilterAbstract implements PluginWebSupport, SystemConfigurablePlugin {
    
    protected Map<String, String> urlMap;
    protected WorkflowUserManager workflowUserManager;
    
    @Override
    public String getName() {
        return "SampleWebFilterPlugin";
    }

    @Override
    public String getVersion() {
        return "8.2.0";
    }
    
    @Override
    public String getLabel() {
        return "Sample Web Filter Plugin";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/"+getName()+".json", null, true, "messages/"+getName());
    }

    @Override
    public String[] getUrlPatterns() {
        return new String[]{"/api/rdr/*"};
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");

        //if (!workflowUserManager.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN)) {
            HttpServletRequest httprequest = (HttpServletRequest) request;
            HttpServletResponse httpresponse = (HttpServletResponse) response;
             
            SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(httprequest, httpresponse);
            String savedUrl = "";
            if (savedRequest != null) {
                savedUrl = savedRequest.getRedirectUrl();
            } else if (httprequest.getHeader("referer") != null) {
                savedUrl = httprequest.getHeader("referer");
            }
             
            String redirectUrl = "";
            String paramValue = "";
            String paramKey = "";
            String requestURI = httprequest.getRequestURI();
            String requestURL = httprequest.getRequestURL().toString();
            boolean redirectFlg = false;
            Object urlProperty = getProperty("urlRepeater");
            if (urlProperty != null) {
                for (Object url : (Object[]) urlProperty) {
                    urlMap = ((Map)url);
                    redirectUrl = (String) urlMap.get("redirectUrl");
                    String matchUrl = (String) urlMap.get("matchUrl");
                    String[] enabledRoles = (String[]) urlMap.get("enabledRoles").split(";");
                    String params = "";
                    if (requestURI.equals(matchUrl) && !requestURL.equals(redirectUrl) && enabledRoles.length > 0) {
                        if (roleMatchesAny(enabledRoles)) {
                            redirectFlg = true;
                        }
                        if (((String) urlMap.get("passParameter")).equals("true")) {
                            Object gridParam = urlMap.get("gridParameter");
                            for (Object param : (Object[]) gridParam) {
                                Map<String, String> gridMap = ((Map)param);
                                paramKey = (String) gridMap.get("redirectParameter");
                                paramValue = httprequest.getParameter((String) gridMap.get("matchParameter"));
                                if (paramValue != null && !paramValue.isEmpty()) {
                                    if (params.isEmpty()) {
                                        params += ("?" + paramKey + "=" + paramValue);
                                    }
                                    else {
                                        params += ("&" + paramKey + "=" + paramValue);
                                    }
                                }
                            }
                            redirectUrl  += params;
                        }
                        break;
                    }
                }
            }
             
            // Only apply for navigating to assignments. Feel free to change this condition for any other menus to login redirect
            if (redirectFlg) {
                //PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                //Map data = new HashMap();
                //data.put("plugin", this);
                //data.put("request", request);
                //data.put("savedUrl", savedUrl);
                //data.put("redirectUrl", redirectUrl);
                //data.put("paramKey", paramKey);
                //data.put("paramValue", paramValue);
                //data.put("username", workflowUserManager.getCurrentUser().getUsername());
                //data.put("password", workflowUserManager.getCurrentUser().getPassword());
                
                //String html = pluginManager.getPluginFreeMarkerTemplate(data, getClassName(), "/templates/SampleWebFilterPlugin.ftl", null);
                
                
                //httpresponse.getWriter().write(html);
                //httpresponse.setContentType("text/html;charset=UTF-8");
                if (isForward(redirectUrl, httprequest)) {
                    if (getPropertyString("debug").equals("true")) {
                        LogUtil.info(getClassName(), "is forwarded to " + redirectUrl);
                    }
                    String forwardUrl = redirectUrl.substring(httprequest.getContextPath().length());
                    httprequest.getRequestDispatcher(forwardUrl).forward(httprequest, httpresponse);
                } else {
                    if (!redirectUrl.startsWith("http://") && !redirectUrl.startsWith("https://")) {
                        String scheme = httprequest.getScheme();
                        redirectUrl = scheme + "://" + redirectUrl;
                    }
                    if (getPropertyString("debug").equals("true")) {
                        LogUtil.info(getClassName(), "is redirected to " + redirectUrl);
                    }
                    httpresponse.sendRedirect(redirectUrl);
                }
                return;
            }
        //}
         
        filterChain.doFilter(request, response);
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("getRoles".equals(action)) {

            try {
                JSONArray jsonArray = new JSONArray();

                workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
                
                jsonArray.put(new JSONObject().put("value", workflowUserManager.ROLE_ADMIN).put("label", "Admin"));
                jsonArray.put(new JSONObject().put("value", workflowUserManager.ROLE_USER).put("label", "Logged in user"));
                jsonArray.put(new JSONObject().put("value", workflowUserManager.ROLE_ANONYMOUS).put("label", "Anonymous"));

                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get roles options Error!");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    /**
     * check if link is internal URL
     * @param redirectUrl
     * @param request
     * @return
     */
    private boolean isForward(String redirectUrl, ServletRequest request) {
        try {
            // 1. Relative links are internal
            if (!redirectUrl.matches("^(?i)(http|https)://.*")) {
                // But filter out bare domains like "www.google.com"
                // Looks like a domain, treat as external
                // e.g. "/page", "dashboard", "?q=1"
                
                return !redirectUrl.matches("^[\\w.-]+\\.[a-z]{2,}.*"); 
            }

            // 2. Parse the URL
            URL url = new URL(redirectUrl);
            String urlHost = url.getHost();
            int urlPort = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();

            // 3. Get the current request host and port
            String requestHost = request.getServerName();
            int requestPort = request.getServerPort();

            // 4. Compare host (and optionally port)
            return urlHost.equalsIgnoreCase(requestHost) && urlPort == requestPort;

        } catch (Exception e) {
            // Invalid URL or something went wrong
            return false;
        }
    }

    /**
     * Check if selected roles correspond to current user role
     * @param rolesEnabled selected roles
     * @return
     */
    private boolean roleMatchesAny(String[] rolesEnabled) {
        for (String role : rolesEnabled) {
            if ((WorkflowUtil.isCurrentUserAnonymous() && WorkflowUtil.getCurrentUsername().equals(role)) || workflowUserManager.isCurrentUserInRole(role)) {
                return true;
            }
        }
        return false;
    }

}