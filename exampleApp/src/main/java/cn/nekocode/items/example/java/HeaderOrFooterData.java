/*
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.items.example.java;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class HeaderOrFooterData {
    private String text;
    private boolean isHeader;
    private boolean isChecked;

    public HeaderOrFooterData(String text) {
        this.text = text;
        this.isHeader = true;
    }

    public HeaderOrFooterData(String text, boolean isChecked) {
        this.text = text;
        this.isHeader = false;
        this.isChecked = isChecked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
