package org.gonevertical.demogwtoauthpopup.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gadgets.client.Gadget;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.gadgets.client.NeedsLockedDomain;
import com.google.gwt.gadgets.client.io.AuthorizationType;
import com.google.gwt.gadgets.client.io.IoProvider;
import com.google.gwt.gadgets.client.io.MethodType;
import com.google.gwt.gadgets.client.io.Response;
import com.google.gwt.gadgets.client.io.ResponseReceivedHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

@ModulePrefs(//
    title = "Demo GWT Gadget OAuth", //
    directory_title = "Demo GWT Gadget OAuth", //
    author = "Brandon Donnelson", //
    author_email = "branflake2267@gmail.com", //
    author_affiliation = "http://gwt-examples.googlecode.com")
@com.google.gwt.gadgets.client.Gadget.UseLongManifestName(false)
@com.google.gwt.gadgets.client.Gadget.AllowHtmlQuirksMode(false)
@com.google.gwt.gadgets.client.Gadget.InjectModulePrefs(files="ModulePrefs.txt")
@com.google.gwt.gadgets.client.Gadget.InjectContent(files="Content.txt")
public class DemoGwtOAuthPopUp extends Gadget<GadgetPreferences> implements NeedsLockedDomain {

  private VerticalPanel vpContent = new VerticalPanel();
  
  /**
   * gadget initialization, then lets start a makerequest to get some data
   */
  protected void init(GadgetPreferences preferences) {
    
    // some simple ui
    HTML html = new HTML("Demo GWT Gadget has loaded. v21");
    VerticalPanel pWidget = new VerticalPanel();
    pWidget.add(vpContent);
    pWidget.add(html);
    RootPanel.get().add(pWidget);
    
    // lets try to get some data
    makeRequest();
  }

  /**
   * if we don't have oauth
   * 
   * @param url
   */
  private void drawPersonalize(final String url) {
    vpContent.clear();
    String s = "<a>Personalize this gadget&nbsp;<img src=\"http://demogwtgadgetoauthpopup.appspot.com/demogwtoauthpopup/images/new.gif\"/></a>";
    HTML h = new HTML(s);
    h.addStyleName("demogadget_personalize");
    vpContent.add(h);
    h.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        open(url);
      }
    });
     
  }

  /**
   * make a request for data, and it may ask us for permission to get it
   */
  private void makeRequest() {
    
    // callback
    ResponseReceivedHandler<String> callback = new ResponseReceivedHandler<String>() {
      public void onResponseReceived(ResponseReceivedEvent<String> event) {
        Response<String> response = event.getResponse();
        processResponse(response);
      }
    };
    
    // extended options to put in google and always oauth vars. The api needs enhancments, and may get this shortly.
    RequestOptionsExtended options = RequestOptionsExtended.newInstanceEx();
    options.setMethodType(MethodType.GET);
    options.setAuthorizationType(AuthorizationType.OAUTH);
    options.setOAuthServiceName("google");
    options.setOAuthUseToken("always");
    
    // make request to url, that will let us in or send back an oauth approval url. This goes through the gadget proxy.
    String url = "http://www.blogger.com/feeds/default/blogs";
    IoProvider.get().makeRequestAsText(url, callback, options);
  }
 
  /**
   * MakeRequest's callback, lets ask to personalize if no oauth url, or we shoudl have some data if its been done already.
   * 
   * @param response
   */
  protected void processResponse(Response<String> response) {
    
    if (response.getStatusCode() == 200 && response.getOauthApprovalUrl() != null) {
      drawPersonalize(response.getOauthApprovalUrl());
      
    } else if (response.getStatusCode() == 200 && response.getOauthApprovalUrl() == null) {
      String s = response.getData();
      drawContent(s);
      
    } else if (response.getStatusCode() != 200) {
      if (response.getErrors() != null) {
        String s = "";
        s += " getStatusCode(): " + response.getStatusCode() + "<br/>\n";
        s += " getText(): " + response.getText() + "<br/>\n";
        s += " getOauthApprovalUrl(): " + response.getOauthApprovalUrl() + "<br/>\n";
        s += " getOauthErrorText(): " + response.getOauthErrorText() + "<br/>\n";
        s += " getOauthError(): " + response.getOauthError() + "<br/>\n";
        s += " getErrors(): <br/>\n";
        JsArrayString ea = response.getErrors();
        for (int i=0; i < response.getErrors().length(); i++) {
          s += ea.get(i) + "<br/>\n";
        }
        vpContent.clear();
        vpContent.add(new HTML(s));
      }
    }
    
  }

  /**
   * draw some data that we retrieved
   * @param s
   */
  protected void drawContent(String s) {
    s = "Your Approved to get blog data: " + s;
    HTML h = new HTML(s);
    vpContent.clear();
    vpContent.add(h);
  }

  /**
   * open window, then start a process to watch for it to close
   * 
   * @param url
   */
  private void open(String url) {
    openWindow(url);
    loop();
  }

  /**
   * open new window
   * 
   * @param url
   * @return
   */
  private final native void openWindow(String url) /*-{
    $wnd.winHandle = $wnd.open(url, "_blank", null);
  }-*/;
  
  /**
   * is window still open
   * 
   * @return
   */
  private final native boolean isWindowClosed() /*-{
    var b = false;
    if ($wnd.winHandle.closed == true) {
      b = true;
    }
    return b;
  }-*/;
  
  /**
   * loop over and over watching for window to close
   */
  private void loop() {
    Timer t = new Timer() {
      public void run() {
        if (isWindowClosed() == true) {
          setWindowClosed();
          return;
        }
        loop();
      }
    };
    t.schedule(100);
  }
  
  /**
   * call this when the window closes
   */
  public void setWindowClosed() {
    Window.alert("setWindowClosed");
    
    // TODO - did we get accesss or not
    
  }
  
}
