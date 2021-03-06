package com.dou361.live.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dou361.live.R;
import com.dou361.live.ui.adapter.RoomMessageAdapter;
import com.dou361.live.ui.listener.MessageViewListener;
import com.dou361.live.ui.listener.OnItemClickRecyclerListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * ========================================
 * <p>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p>
 * 作 者：陈冠明
 * <p>
 * 个人网站：http://www.dou361.com
 * <p>
 * 版 本：1.0
 * <p>
 * 创建日期：2016/10/5 20:07
 * <p>
 * 描 述：房间聊天布局
 * <p>
 * <p>
 * 修订历史：
 * <p>
 * ========================================
 */
public class RoomMessagesView extends RelativeLayout {

    /**
     * 环信聊天对象
     */
    private EMConversation conversation;
    /**
     * 消息适配器
     */
    RoomMessageAdapter adapter;
    /**
     * 消息列表
     */
    RecyclerView recyclerView;
    /**
     * 输入面板
     */
    View sendContainer;
    /**
     * 编辑框
     */
    MentionEditText editText;
    /**
     * 发送
     */
    Button sendBtn;
    /**
     * 关闭
     */
    ImageView closeView;
    /**
     * 弹幕
     */
    ImageView danmuImage;
    /**
     * 房间监听
     */
    MessageViewListener messageViewListener;
    /**
     * 消息列表
     */
    List<EMMessage> list = new ArrayList<EMMessage>();
    /**
     * 是否是弹幕
     */
    private boolean isDanmuShow = false;
    /**
     * 艾特的某人的集合
     */
    private List<String> atList = new ArrayList<String>();


    public RoomMessagesView(Context context) {
        super(context);
        init(context, null);
    }

    public RoomMessagesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoomMessagesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.live_widget_room_messages, this);
        recyclerView = (RecyclerView) findViewById(R.id.listview);
        editText = (MentionEditText) findViewById(R.id.edit_text);
        sendBtn = (Button) findViewById(R.id.btn_send);
        closeView = (ImageView) findViewById(R.id.close_image);
        sendContainer = findViewById(R.id.container_send);
        danmuImage = (ImageView) findViewById(R.id.danmu_image);
    }

    /**
     * 获取输入框
     */
    public MentionEditText getInputView() {
        return editText;
    }

    /**
     * 获取艾特的集合
     */
    public List<String> getAtList() {
        return atList;
    }

    public void init(String chatroomId) {
        conversation = EMClient.getInstance().chatManager().getConversation(chatroomId, EMConversation.EMConversationType.ChatRoom, true);
        List<EMMessage> temp = conversation.getAllMessages();
        if (temp != null && temp.size() > 0) {
            list.clear();
            list.addAll(temp);
        }
        adapter = new RoomMessageAdapter(getContext(), list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickRecyclerListener() {
            @Override
            public void onItemClick(View view, int postion) {
                if (messageViewListener != null) {
                    EMMessage message = list.get(postion);
                    messageViewListener.onItemClickListener(0, message.getFrom());
                }
            }
        });
        sendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageViewListener != null) {
                    if (TextUtils.isEmpty(editText.getText())) {
                        Toast.makeText(getContext(), "文字内容不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    /**这里是@的集合*/
                    List<String> temp = editText.getMentionList(true);
                    if (temp != null && temp.size() > 0) {
                        atList.clear();
                        atList.addAll(temp);
                    }
                    messageViewListener.onMessageSend(editText.getText().toString());
                    editText.setText("");
                }
            }
        });
        closeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowInputView(false);
                if (messageViewListener != null) {
                    messageViewListener.onHiderBottomBar();
                }
            }
        });

        danmuImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (danmuImage.isSelected()) {
                    danmuImage.setSelected(false);
                    isDanmuShow = false;
                } else {
                    danmuImage.setSelected(true);
                    isDanmuShow = true;
                }
            }
        });
        /**输入框文本输入监听*/
        editText.setMentionTextColor(Color.RED); //optional, set highlight color of mention string
        editText.setPattern("@[\\u4e00-\\u9fa5\\w\\-]+"); //optional, set regularExpression
        /**自动补全*/
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ("@".equals(editText.getText().toString())) {
                    editText.setText("@全体成员 ");
                    editText.setSelection(editText.getText().length());
                }
            }
        });

    }

    /**
     * 艾特功能中设置艾特的人
     */
    public void setReplyer(String txt) {
        editText.setText("@" + txt + " ");
        editText.setSelection(editText.getText().length());
    }

    public void setShowInputView(boolean showInputView) {
        if (showInputView) {
            sendContainer.setVisibility(View.VISIBLE);
        } else {
            sendContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void setMessageViewListener(MessageViewListener messageViewListener) {
        this.messageViewListener = messageViewListener;
    }

    /**
     * 刷新
     */
    public void refresh() {
        List<EMMessage> temp = conversation.getAllMessages();
        if (temp != null && temp.size() > 0) {
            list.clear();
            list.addAll(temp);
        }
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 刷新并定位到最后一条
     */
    public void refreshSelectLast() {
        if (adapter != null) {
            refresh();
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    /**
     * 是否开始弹幕（飘屏）
     */
    public boolean isDanmuShow() {
        return isDanmuShow;
    }
}
