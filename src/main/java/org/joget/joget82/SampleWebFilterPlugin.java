package org.joget.joget82;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.PluginWebFilterAbstract;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.SystemConfigurablePlugin;
import org.joget.workflow.util.WorkflowUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class SampleWebFilterPlugin extends PluginWebFilterAbstract implements SystemConfigurablePlugin {
    
    protected Map<String, String> urlMap;
    
    @Override
    public String getName() {
        return "SampleWebFilterPlugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
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
        //return new String[]{"/" + getShortLink() + "/*"};
        return new String[]{"/rdr/*"};
    }

    //public String getShortLink() {
    //    if (!getPropertyString("shortLink").isEmpty()){
    //        return getPropertyString("shortLink");
    //    }
    //    else {
    //        return "rdr";
    //    }
    //}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
             
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
             
            if (savedUrl.contains("ulogin")) {
                savedUrl = savedUrl.replaceAll("ulogin", "userview");
            }
            String redirectUrl = "";
            String paramValue = "";
            String paramKey = "";
            String requestURI = httprequest.getRequestURI();
            String requestURL = httprequest.getRequestURL().toString();
            boolean redirectFlg = false;
            Object urlProperty = getProperty("url");
            if (urlProperty != null) {
                for (Object url : (Object[]) urlProperty) {
                    urlMap = ((Map)url);
                    redirectUrl = (String) urlMap.get("redirectUrl");
                    String matchUrl = (String) urlMap.get("matchUrl");
                    if (requestURI.equals(matchUrl) && !requestURL.equals(redirectUrl)) {
                        redirectFlg = true;
                        if (((String) urlMap.get("passParameter")).equals("true")) {
                            paramKey = (String) urlMap.get("redirectParameter");
                            paramValue = httprequest.getParameter((String) urlMap.get("matchParameter"));
                            if (paramValue != null && !paramValue.isEmpty()) {
                                redirectUrl += ("?" + paramKey + "=" + paramValue); 
                            }
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
                httpresponse.sendRedirect(redirectUrl);
                return;
            }
        //}
         
        filterChain.doFilter(request, response);
    }
}