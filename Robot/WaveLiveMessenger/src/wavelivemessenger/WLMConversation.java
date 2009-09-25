// Copyright 2009, Acknack Ltd. All rights reserved.
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.


package wavelivemessenger;

import java.util.ArrayList;

public class WLMConversation {
	private ArrayList<WLMPost> posts;
	private final String participant;
	
	public WLMConversation(String participant) {
		this.participant = participant;
		this.posts = new ArrayList<WLMPost>();
	}

	public ArrayList<WLMPost> getPosts() {
		setAllRead();
		return posts;
	}

	public void addPost(WLMPost p) {
		posts.add(p);
	}

	public String getParticipant() {
		return participant;
	}
	
	public void setAllRead() {
		for(int i = 0; i < posts.size(); i++) {
			posts.get(i).readMessage();
		}
	}
	
	public int getUnreadCount() {
		int c = 0;
		for(int i = 0; i < posts.size(); i++) {
			if(posts.get(i).isRead() == false) {
				c++;
			}
		}
		return c;
	}
}
