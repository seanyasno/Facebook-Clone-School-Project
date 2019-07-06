import asyncio
import websockets
import json
import base64
import time
import os
from pyfcm import FCMNotification

def send_notification(username, message_title, message_body):
    f = open('users_database.json')
    s = f.read()

    users = json.loads(s)
    
    registration_id = ''
    for user in users['users']:
        if user['username'] == username:
            if len(user['registration_ids']) > 0:
                registration_id = user['registration_ids'][0]['token']
            break

    f.close()
    
    API_KEY = "AIzaSyD0sTq0FYCD5rOkb4j-O6XQs3Hq3ucsGik"

    push_service = FCMNotification(api_key=API_KEY)

    result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)

    print (result)

def check_users_condition(username, otherUsername):
    f = open("users_database.json", "r")
    s = f.read()

    print("Condition users: " + username + " , " + otherUsername)

    users = json.loads(s)
    
    for user in users["users"]:
        if user["username"] == username:
            '''
            Checking if the user sent a friend request to the other user
            '''
            for otherUser in user["waiting_friend_requests"]:
                if otherUser["username"] == otherUsername:
                    return 'SENTREQUEST'
            '''
            Cheking if the user needs to confirm or reject the friend request that the other user has sent
            '''
            for otherUser in user["friend_requests"]:
                if otherUser["username"] == otherUsername:
                    return 'FRIENDREQUEST'
            '''
            Checking if both users are friends
            '''
            for otherUser in user["friends"]:
                if otherUser["username"] == otherUsername:
                    return 'FRIENDS'
            '''
            Both users are not friends
            '''
    return 'NOTFRIENDS'


    s.close()

def add_comment_to_database(comment, username, post_id):
    f = open("users_post_database.json", "r")
    s = f.read()
    comment = json.loads(comment)
    posts = json.loads(s)

    index1 = 0   
    for user in posts['users']:
        index2 = 0
        for post in user['posts']:
            if post['upload_time'] == post_id:
                posts['users'][index1]['posts'][index2]['comments'].append(comment)
                send_notification(user['username'], 'Post update', username + ' has comment on your post.')
            index2+=1
        index1+=1
    f.close()

    f = open("users_post_database.json", "w")
    f.write(json.dumps(posts))
    f.close()

    print('added comment to database')

def add_post_to_database(post, username, img):
    f = open("users_post_database.json", "r")
    s = f.read()

    posts = json.loads(s)

    added = False
    index = 0
    for user in posts['users']:
        if user['username'] == username:
            posts['users'][index]['posts'].append(post)
            added = True
        index+=1
    
    if not added:
        a = []
        a.append(post)
        posts['users'].append({"username":username, "posts":a})

    # try:
    #     (next(user for user in posts['users'] if user['username'] == username))['posts'].append(post)
    # except:
    #     posts['users']

    f.close()
    f = open("users_post_database.json", "w")
    f.write(json.dumps(posts))
    f.close()

def get_likes_by_post(post):
    f = open("users_post_database.json", "r")
    s = f.read()

    posts = json.loads(s)
    for user in posts['users']:
        for p in user['posts']:
            if p['upload_time'] == post['upload_time']:
                return p['likes']
    return 0

def add_like_to_post(post, username):
    f = open("users_post_database.json", "r")
    s = f.read()

    posts = json.loads(s)

    index1 = 0
    for user in posts['users']:
        index2 = 0
        for p in user['posts']:
            if p['upload_time'] == post['upload_time']:
                f.close()
                f = open("users_post_database.json", "w")

                posts['users'][index1]['posts'][index2]['likes'] += 1
                posts['users'][index1]['posts'][index2]['users'].append(username)

                f.write(json.dumps(posts))

                f.close()

                send_notification(user['username'], "Post update", username + " has liked your photo.")

                break

            index2 += 1
        index1 += 1
    f.close()

def sub_like_to_post(post, username):
    f = open("users_post_database.json", "r")
    s = f.read()

    posts = json.loads(s)

    index1 = 0
    for user in posts['users']:
        index2 = 0
        for p in user['posts']:
            if p['upload_time'] == post['upload_time']:
                f.close()
                f = open("users_post_database.json", "w")

                posts['users'][index1]['posts'][index2]['likes'] -= 1
                posts['users'][index1]['posts'][index2]['users'].remove(username)

                f.write(json.dumps(posts))

                f.close()
            index2 += 1
        index1 += 1
    f.close()

def get_user_by_username(username):
    f = open("users_database.json", "r")
    s = f.read()

    users = json.loads(s)
    for user in users['users']:
        if username == user['username']:
            f.close()
            return user
    f.close()
    return None

def check_user(username, password):
    f = open("users_database.json", "r")
    s = f.read()

    users = json.loads(s)
    users = users['users']

    '''
    it checks in the user database if the given user 
    matches another use in the database
    '''
    for user in users:
        if username == user['username'] and password == user['password']:
            f.close()
            return True
    f.close()
    return False

def add_user(user):
    f = open("users_database.json", "r")
    s = f.read()

    '''
    it converts the json file input into an array
    than it adds an element to the array and after that
    it converts it back into json file
    '''
    users = json.loads(s)
    users['users'].append(user)
    users = json.dumps(users)

    f.close()
    f = open("users_database.json", "w")
    f.write(users)
    f.close()

async def mainloop(ws, path):
    command = await ws.recv()

    # print (path)

    print("command: " + command)

    if command == "login":
        user = await ws.recv()
        user = json.loads(user)
        print(user)

        if check_user(user['username'], user['password']):
            print('correct')
            await ws.send('correct')
        else:
            print('incorrect')
            await ws.send('incorrect')

    if command == "register":
        user = await ws.recv()
        user = json.loads(user)
        print(user['username'] + " Added to database")
        if not os.path.exists('users\\'+user['username']):
            os.makedirs('users\\'+user['username'])
        add_user(user)

    if command == "getuser":
        username = await ws.recv()
        print(username)
        user = get_user_by_username(username)

        print(user)
        await ws.send(str(user))

    if command == "postlike":
        username = await ws.recv()
        add = await ws.recv()
        print("USERNAME " + username)
        post = await ws.recv()
        post = json.loads(post)
        print(post)
        print('boiiii ' + add)
        if add == "yes":
            add_like_to_post(post, username)
        elif add == "no":
            sub_like_to_post(post, username)
        likes = get_likes_by_post(post)
        await ws.send(str(likes))
        print("sent " + str(likes))

    if command == "postloader":
        u = await ws.recv()
        f = open("users_database.json", "r")
        s = f.read()

        print("u: " + u)

        users = json.loads(s)
        post_users = []
        post_users.append(u)

        for user in users["users"]:
            if user["username"] == u:
                for friend in user["friends"]:
                    post_users.append(friend['username'])
        f.close()

        f = open("users_post_database.json", "r")
        s = f.read()

        users = json.loads(s)
        posts = []
        
        print('FREIND USERS : ' + str(post_users))

        for user in users['users']:
            if user['username'] in post_users:
                for post in user['posts']:
                    if post['image'] != '':
                        ff = open(post['image'], 'rb')
                        post['image'] = base64.encodestring(ff.read()).decode('utf-8')
                        ff.close()
                    print(post)
                    posts.append(post)
        num_of_posts = str(len(posts))
        print("LENGTH OF POSTS : " + str(num_of_posts))
        await ws.send(num_of_posts)
        ok = "ok"
        for post in posts:
            # print(json.dumps(post))
            if (ok == "ok"):
                await ws.send(str(json.dumps(post)))
                ok = ""
            ok = await ws.recv()

        f.close()
        print('done')

    if command == "postuploader":
        post = await ws.recv()
        username = await ws.recv()
        img = await ws.recv()
        post = json.loads(post)

        img_name = post['image']

        if img_name != '':
            f = open(img_name, 'wb')
            f.write(base64.b64decode(img))
            f.close()

        print(post)
        add_post_to_database(post, username, img)

        await ws.send("done")

    if command == "commentsloader":
        post_id = await ws.recv()

        f = open("users_post_database.json", "r")
        s = f.read()

        posts = json.loads(s)

        for user in posts['users']:
            for post in user['posts']:
                if post['upload_time'] == post_id:
                    # print(json.dumps(post['comments']))
                    await ws.send(json.dumps(post['comments']))
        f.close()

    if (command == "addcomment"):
        post_id = await ws.recv()
        username = await ws.recv()
        comment = await ws.recv()
        add_comment_to_database(comment, username, post_id)

    if (command == "search"):
        username = await ws.recv()
        print(username)

        f = open("users_database.json", "r")
        s = f.read()

        posts = json.loads(s)

        users = []

        for user in posts["users"]:
            if user['first_name'].lower() in username.lower() or user['last_name'].lower() in username.lower():
                print('yess')
                users.append(user)
        await ws.send(json.dumps(users))

        f.close()

    if (command == "userCondition"):
        username = await ws.recv()
        otherUsername = await ws.recv()

        condition = check_users_condition(username, otherUsername)
        await ws.send(condition)
        print("The condition is " +  condition)

    if command == "sendFriendRequest":
        username = await ws.recv()
        otherusername = await ws.recv()

        f = open("users_database.json", "r")
        s = f.read()

        users = json.loads(s)
        index = 0

        for user in users["users"]:
            if user["username"] == username:
                users["users"][index]["waiting_friend_requests"].append({"username":otherusername})
            elif user["username"] == otherusername:
                users["users"][index]["friend_requests"].append({"username":username})
            index+=1

        f.close()

        f = open("users_database.json", "w")
        f.write(json.dumps(users))
        f.close()
        print('a new friend request has been added to ' + username + ' and ' + otherusername)
        await ws.send("true")

    if command == "cancelFriendRequest":
        username = await ws.recv()
        otherusername = await ws.recv()

        f = open("users_database.json", "r")
        s = f.read()

        users = json.loads(s)
        index = 0

        for user in users["users"]:
            if user["username"] == username:
                users["users"][index]["waiting_friend_requests"].remove({"username":otherusername})
            elif user["username"] == otherusername:
                users["users"][index]["friend_requests"].remove({"username":username})
            index+=1

        f.close()

        f = open("users_database.json", "w")
        f.write(json.dumps(users))
        f.close()
        print('a friend request to ' + otherusername + ' has been canceled by ' + username)
        await ws.send("true")

    if command == "confirmFriendRequest":
        username = await ws.recv()
        otherusername = await ws.recv()

        f = open("users_database.json", "r")
        s = f.read()

        users = json.loads(s)
        
        index = 0
        for user in users["users"]:
            if user["username"] == username:
                users["users"][index]["friend_requests"].remove({"username":otherusername})
                users["users"][index]["friends"].append({"username":otherusername})
            elif user["username"] == otherusername:
                users["users"][index]["waiting_friend_requests"].remove({"username":username})
                users["users"][index]["friends"].append({"username":username})
            index+=1
        f.close()

        f = open("users_database.json", "w")
        f.write(json.dumps(users))
        f.close()
        print(username + " confirmed " + otherusername + " as a friend")
        await ws.send("true")

    if command == "rejectFriendRequest":
        username = await ws.recv()
        otherusername = await ws.recv()

        f = open("users_database.json", "r")
        s = f.read()

        users = json.loads(s)
        
        index = 0
        for user in users["users"]:
            if user["username"] == username:
                users["users"][index]["friend_requests"].remove({"username":otherusername})
            elif user["username"] == otherusername:
                users["users"][index]["waiting_friend_requests"].remove({"username":username})
            index+=1
        f.close()

        f = open("users_database.json", "w")
        f.write(json.dumps(users))
        f.close()
        print(username + " rejected " + otherusername + " as a friend")
        await ws.send("true")

    if command == "removeUserAsFriend":
        username = await ws.recv()
        otherusername = await ws.recv()

        f = open("users_database.json", "r")
        s = f.read()

        users = json.loads(s)
        
        index = 0
        for user in users["users"]:
            if user["username"] == username:
                users["users"][index]["friends"].remove({"username":otherusername})
            elif user["username"] == otherusername:
                users["users"][index]["friends"].remove({"username":username})
            index+=1
        f.close()

        f = open("users_database.json", "w")
        f.write(json.dumps(users))
        f.close()
        print(username + " removed " + otherusername + " as a friend")
        await ws.send("true")

    if command == "changeProfileImage":
        username = await ws.recv()
        img = await ws.recv()
        
        try:
            f = open('users\\'+username+'\\profile_image.jpg', 'wb')
            f.write(base64.b64decode(img))
            f.close()
            await ws.send('true')
        except:
            await ws.send('false')
    
    if command == "changeBackgroundImage":
        username = await ws.recv()
        img = await ws.recv()

        try:
            f = open('users\\'+username+'\\background_image.jpg', 'wb')
            f.write(base64.b64decode(img))
            f.close()
            await ws.send('true')
        except:
            await ws.send('false')

    if command == "getProfileImage":
        username = await ws.recv()
        if not os.path.exists('users\\'+username+'\\profile_image.jpg'):
            await ws.send('noimage')
        else:
            ff = open('users\\'+username+'\\profile_image.jpg', 'rb')
            img = base64.encodestring(ff.read()).decode('utf-8')
            ff.close()
            await ws.send(img)

    if command == "getBackgroundImage":
        username = await ws.recv()
        if not os.path.exists('users\\'+username+'\\background_image.jpg'):
            await ws.send('noimage')
        else:
            ff = open('users\\'+username+'\\background_image.jpg', 'rb')
            img = base64.encodestring(ff.read()).decode('utf-8')
            ff.close()
            await ws.send(img)

    if command == "add_registration_id":
        username = await ws.recv()
        token = await ws.recv()

        f = open('users_database.json', 'r')
        s = f.read()

        users = json.loads(s)

        index = 0
        for user in users['users']:
            if user['username'] == username:
                is_in = False
                for t in user['registration_ids']:
                    if t == token:
                        is_in = True
                        break
                if not is_in:
                    del users['users'][index]["registration_ids"][:]
                    users['users'][index]["registration_ids"].append({"token":token})
                break
            index+=1

        f.close()

        f = open("users_database.json", "w")
        f.write(json.dumps(users))
        f.close()
        print ("added " + username + "'s token to database")


'''
serve() returns an awaitable. 
Awaiting it yields an instance of WebSocketServer which provides close() 
and wait_closed() methods for terminating the server and cleaning up its resources.
'''
start_server = websockets.serve(mainloop, '172.19.9.19', 8080)

asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever() 