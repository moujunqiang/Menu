package com.android.menu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * text
     */
    private TextView mText;
    private Button mBtn1;
    private Button mBtn2;
    private ListView mList;
    List<String> list = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initView();
    }

    // 让菜单同时显示图标和文字
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e("menu", "onCreateOptionsMenu");
        // 在菜单中添加菜单项
        menu.add(Menu.FIRST, 1, 1, "菜单1").setIcon(R.mipmap.ic_launcher);
        menu.add(Menu.FIRST, 2, 2, "菜单2").setIcon(R.mipmap.ic_launcher);
        menu.add(Menu.FIRST, 3, 2, "菜单3").setIcon(R.mipmap.ic_launcher);
        menu.add(Menu.FIRST, 4, 2, "菜单4").setIcon(R.mipmap.ic_launcher);
        menu.add(Menu.FIRST, 5, 2, "菜单5").setIcon(R.mipmap.ic_launcher);
        menu.add(Menu.FIRST, 6, 2, "菜单6").setIcon(R.mipmap.ic_launcher);
        menu.add(Menu.FIRST, 7, 2, "菜单7").setIcon(R.mipmap.ic_launcher);

        //此方法必须返回true，否则不予显示
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e("menu", "onOptionsItemSelected");

        switch (item.getItemId()) {
            case 1:
                Toast.makeText(this, "点击了第一个", Toast.LENGTH_SHORT).show();
                return true; //返回true，将结束点击事件，不会进入下一级菜单
            case 2:
                Toast.makeText(this, "点击了第二个", Toast.LENGTH_SHORT).show();

                return true;
            case 3:
                Toast.makeText(this, "点击了第三个", Toast.LENGTH_SHORT).show();

                return true;

            default:
                return super.onOptionsItemSelected(item); //不返回true，避免截断菜单项的点击事件
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == 21) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int select = 0;
            for (int i = 0; i < list.size(); i++) {
                if (mList.getItemIdAtPosition(i) == menuInfo.id) {
                    select = i;
                    break;
                }
            }
            String selectString = mList.getItemAtPosition(select).toString();
            StringBuilder stringBuilder = new StringBuilder(selectString);
            switch (item.getItemId()) {
                case 21:
                    stringBuilder.append("红色");
                    break;
                case 22:
                    stringBuilder.append("蓝色");

                    break;
                case 23:
                    stringBuilder.append("绿色");
                    break;
            }
            list.set(select, stringBuilder.toString());
            adapter.notifyDataSetChanged();
        }else {
            switch (item.getItemId()) {
                case 11:
                    if (item.isChecked()) {
                        item.setChecked(false);
                    } else {
                        item.setChecked(true);
                    }

                    return true;
                case 12:
                    if (item.isChecked()) {
                        item.setChecked(false);
                    } else {
                        item.setChecked(true);
                    }

                    return true;
                case 13:
                    if (item.isChecked()) {
                        item.setChecked(false);
                    } else {
                        item.setChecked(true);
                    }
                    return true;

            }
        }




        //   mList.setAdapter(adapter);
        return true;
    }

    private void initView() {
        mText = (TextView) findViewById(R.id.text);
        mBtn1 = (Button) findViewById(R.id.btn_1);
        mBtn1.setOnClickListener(this);
        mBtn2 = (Button) findViewById(R.id.btn_2);
        mBtn2.setOnClickListener(this);
        mList = (ListView) findViewById(R.id.list);
        list.add("我喜欢");
        list.add("我讨厌");
        list.add("我不在乎");
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);

        mList.setAdapter(adapter);
        registerForContextMenu(mList);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_1:
                PopupMenu popupMenu = new PopupMenu(MenuActivity.this, v);//1.实例化PopupMenu
                Menu menu = popupMenu.getMenu();
                // 创建子菜单
                SubMenu subMenu1 = menu.addSubMenu(1, 1, 1, "颜色");
                //创建子菜单项
                subMenu1.add(1, 101, 1, "红色");
                subMenu1.add(1, 102, 1, "蓝色");
                subMenu1.add(1, 103, 1, "绿色");

                // 创建子菜单2
                SubMenu subMenu2 = menu.addSubMenu(2, 2, 1, "字体");
                //创建子菜单项
                subMenu2.add(1, 104, 1, "直接关闭");
                subMenu2.add(2, 105, 2, "稍后重启");
                //3.为弹出菜单设置点击监听
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 101:
                                mText.setTextColor(Color.RED);
                                return true;
                            case 102:
                                mText.setTextColor(Color.BLUE);
                                return true;
                            case 103:
                                mText.setTextColor(Color.GREEN);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();//4.显示弹出菜单
                break;
            case R.id.btn_2:
                registerForContextMenu(mBtn2);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        Log.e("menu", "onCreateContextMenu");

        switch (v.getId()) {
            case R.id.btn_2:
                menu.add(11, 11, 11, "Traffic").setCheckable(true).setChecked(true);
                menu.add(12, 12, 12, "Map").setCheckable(true).setChecked(false);
                menu.add(31, 13, 13, "Satellite").setCheckable(true).setChecked(false);
                menu.setGroupCheckable(0, true, true);

                break;
            case R.id.list:
                menu.add(21, 21, 11, "红色");
                menu.add(21, 22, 12, "蓝色");
                menu.add(21, 23, 13, "绿色");
                break;

            default:
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }
}