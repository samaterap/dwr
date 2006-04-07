/*
 * Copyright 2005 Joe Walker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.directwebremoting.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.directwebremoting.Container;
import org.directwebremoting.ConverterManager;
import org.directwebremoting.MarshallException;
import org.directwebremoting.OutboundContext;
import org.directwebremoting.OutboundVariable;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ScriptSessionManager;
import org.directwebremoting.WebContext;
import org.directwebremoting.util.SwallowingHttpServletResponse;
import org.directwebremoting.util.VersionUtil;

/**
 * A default implementation of WebContext
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class DefaultWebContext implements WebContext
{
    /**
     * Create a new DefaultWebContext
     * @param request The incoming http request
     * @param response The outgoing http reply
     * @param config The servlet configuration
     * @param context The servlet context
     * @param container The IoC container
     * @see org.directwebremoting.WebContextBuilder#set(HttpServletRequest, HttpServletResponse, ServletConfig, ServletContext, Container)
     */
    public DefaultWebContext(HttpServletRequest request, HttpServletResponse response, ServletConfig config, ServletContext context, Container container)
    {
        this.request = request;
        this.response = response;
        this.config = config;
        this.context = context;
        this.container = container;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#setCurrentPageInformation(java.lang.String, java.lang.String)
     */
    public void setCurrentPageInformation(String page, String scriptSessionId)
    {
        this.scriptSessionId = scriptSessionId;
        this.page = page;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getCurrentPage()
     */
    public String getCurrentPage()
    {
        return page;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getScriptSession()
     */
    public ScriptSession getScriptSession()
    {
        return getScriptSessionManager().getScriptSession(scriptSessionId, page);
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getScriptSessionsByPage(java.lang.String)
     */
    public Collection getScriptSessionsByPage(String otherPage)
    {
        return getScriptSessionManager().getScriptSessionsByPage(otherPage);
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getAllScriptSessions()
     */
    public Collection getAllScriptSessions()
    {
        return getScriptSessionManager().getAllScriptSessions();
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getContainer()
     */
    public Container getContainer()
    {
        return container;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getSession()
     */
    public HttpSession getSession()
    {
        return request.getSession();
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getSession(boolean)
     */
    public HttpSession getSession(boolean create)
    {
        return request.getSession(create);
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getServletConfig()
     */
    public ServletConfig getServletConfig()
    {
        return config;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getServletContext()
     */
    public ServletContext getServletContext()
    {
        return context;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getHttpServletRequest()
     */
    public HttpServletRequest getHttpServletRequest()
    {
        return request;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getHttpServletResponse()
     */
    public HttpServletResponse getHttpServletResponse()
    {
        return response;
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#forwardToString(java.lang.String)
     */
    public String forwardToString(String url) throws ServletException, IOException
    {
        StringWriter sout = new StringWriter();
        StringBuffer buffer = sout.getBuffer();

        HttpServletResponse realResponse = getHttpServletResponse();
        HttpServletResponse fakeResponse = new SwallowingHttpServletResponse(realResponse, sout);

        HttpServletRequest realRequest = getHttpServletRequest();
        realRequest.setAttribute(WebContext.ATTRIBUTE_DWR, Boolean.TRUE);

        getServletContext().getRequestDispatcher(url).forward(realRequest, fakeResponse);

        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#toJavascript(java.lang.Object)
     */
    public OutboundVariable toJavascript(Object data) throws MarshallException
    {
        return getConverterManager().convertOutbound(data, new OutboundContext());
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.WebContext#getVersion()
     */
    public String getVersion()
    {
        return VersionUtil.getVersion();
    }

    /**
     * Internal helper for getting at a ScriptSessionManager
     * @return Our ScriptSessionManager
     */
    private ScriptSessionManager getScriptSessionManager()
    {
        if (sessionManager == null)
        {
            sessionManager = (ScriptSessionManager) container.getBean(ScriptSessionManager.class.getName());
        }
        
        return sessionManager;
    }

    /**
     * Internal helper for getting at a ConverterManager
     * @return Our ConverterManager
     */
    private ConverterManager getConverterManager()
    {
        if (converterManager == null)
        {
            converterManager = (ConverterManager) container.getBean(ConverterManager.class.getName());
        }
        
        return converterManager;
    }

    /**
     * The unique ID (like a session ID) assigned to the current page
     */
    private String scriptSessionId = null;

    /**
     * The URL of the current page
     */
    private String page = null;

    /**
     * The HttpServletRequest associated with the current request
     */
    private HttpServletRequest request = null;

    /**
     * The HttpServletResponse associated with the current request
     */
    private HttpServletResponse response = null;

    /**
     * The ServletConfig associated with the current request
     */
    private ServletConfig config = null;

    /**
     * The ServletContext associated with the current request
     */
    private ServletContext context = null;

    private Container container = null;
    private ScriptSessionManager sessionManager = null;
    private ConverterManager converterManager = null;
}