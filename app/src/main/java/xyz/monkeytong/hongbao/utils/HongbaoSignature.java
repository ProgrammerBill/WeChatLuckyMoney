package xyz.monkeytong.hongbao.utils;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

import xyz.monkeytong.hongbao.R;
import xyz.monkeytong.hongbao.services.HongbaoService;

/**
 * Created by Zhongyi on 1/21/16.
 */
public class HongbaoSignature {
    public String sender, content, time, contentDescription = "", commentString;
    public boolean others;
    private static String TAG = "HongbaoService";
    private static final String MESSAGE_HAS_RECEIVED = "已被领完";
    private static final String MESSAGE_HAS_RECEIVED_EXTRA = "已领取";

    public boolean generateSignature(AccessibilityNodeInfo node, String excludeWords) {
        try {
            /* The hongbao container node. It should be a LinearLayout. By specifying that, we can avoid text messages. */
            AccessibilityNodeInfo hongbaoNode = node.getParent();
            if (!"android.widget.LinearLayout".equals(hongbaoNode.getClassName())){
				return false;
			}

            /* The text in the hongbao. Should mean something. */
            String hongbaoContent = hongbaoNode.getChild(0).getText().toString();
            for(int i = 0; i < hongbaoNode.getChildCount(); i++){
                if(hongbaoNode.getChild(i).getText() == null) continue;
                String Content = hongbaoNode.getChild(i).getText().toString();
                if(Content.equals(MESSAGE_HAS_RECEIVED) || Content.equals(MESSAGE_HAS_RECEIVED_EXTRA)){
                    return false;
                }
            }

            /* Check the user's exclude words list. */
            String[] excludeWordsArray = excludeWords.split(" +");
            for (String word : excludeWordsArray) {
                if (word.length() > 0 && hongbaoContent.contains(word)){
					return false;
				}
            }

            /* The container node for a piece of message. It should be inside the screen.
                Or sometimes it will get opened twice while scrolling. */
            AccessibilityNodeInfo messageNode = hongbaoNode.getParent();

            Rect bounds = new Rect();
            messageNode.getBoundsInScreen(bounds);
            if (bounds.top < 0){
                return false;
			}

            /* The sender and possible timestamp. Should mean something too. */
			String[] hongbaoInfo = getSenderContentDescriptionFromNode(messageNode);

            /* So far we make sure it's a valid new coming hongbao. */
            this.sender = hongbaoInfo[0];
            this.time = hongbaoInfo[1];
            this.content = hongbaoContent;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getSignature(this.sender, this.content, this.time);
    }

    private String getSignature(String... strings) {
        String signature = "";
        for (String str : strings) {
            if (str == null) return null;
            signature += str + "|";
        }

        return signature.substring(0, signature.length() - 1);
    }

    public String getContentDescription() {
        return this.contentDescription;
    }

    public void setContentDescription(String description) {
        this.contentDescription = description;
    }

    private String[] getSenderContentDescriptionFromNode(AccessibilityNodeInfo node) {
        int count = node.getChildCount();
        String[] result = {"unknownSender", "unknownTime"};
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo thisNode = node.getChild(i);
            if ("android.widget.ImageView".equals(thisNode.getClassName()) && "unknownSender".equals(result[0])) {
                CharSequence contentDescription = thisNode.getContentDescription();
                if (contentDescription != null)
                    result[0] = contentDescription.toString().replaceAll("头像$", "");
            } else if ("android.widget.TextView".equals(thisNode.getClassName()) && "unknownTime".equals(result[1])) {
                CharSequence thisNodeText = thisNode.getText();
                if (thisNodeText != null)
                    result[1] = thisNodeText.toString();
            }
        }
        return result;
    }

    public void cleanSignature() {
        this.content = "";
        this.time = "";
        this.sender = "";
    }

}
