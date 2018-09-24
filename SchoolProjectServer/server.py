import asyncio
import websockets
import json

def add_post_to_database(post):
    f = open("posts_database.json", "r")
    s = f.read()

    posts = json.loads(s)
    posts['posts'].append(post)

    f.close()
    f = open("posts_database.json", "w")
    f.write(json.dumps(posts))
    f.close()


def get_likes_by_post(post):
    f = open("posts_database.json", "r")
    s = f.read()

    posts = json.loads(s)
    index = 0
    for p in posts['posts']:
        if p['id'] == post['id'] and p['user'] == post['user']:
            f.close()
            return posts['posts'][index]['likes']
        index += 1

    f.close()
    return 0

def add_like_to_post(post, username):
    f = open("posts_database.json", "r")
    s = f.read()

    posts = json.loads(s)

    index = 0
    for p in posts['posts']:
        if p['id'] == post['id'] and p['user'] == post['user']:
            f.close()
            print(posts['posts'][index]['likes'])
            f = open("posts_database.json", "w")

            posts['posts'][index]['likes'] += 1
            print(posts['posts'][index]['likes'])
            user = get_user_by_username(username)
            posts['posts'][index]['users'].append(user['username'])

            f.write(json.dumps(posts))

            f.close()
            return
        index += 1
    f.close()


def sub_like_to_post(post, username):
    f = open("posts_database.json", "r")
    s = f.read()

    posts = json.loads(s)

    index = 0
    for p in posts['posts']:
        if p['id'] == post['id'] and p['user'] == post['user']:
            f.close()
            print(posts['posts'][index]['likes'])
            f = open("posts_database.json", "w")

            posts['posts'][index]['likes'] -= 1
            print(posts['posts'][index]['likes'])
            user = get_user_by_username(username)
            posts['posts'][index]['users'].remove(user['username'])
            
            f.write(json.dumps(posts))

            f.close()
            return
        index += 1
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
        if add == "yes":
            add_like_to_post(post, username)
        elif add == "no":
            sub_like_to_post(post, username)
        likes = get_likes_by_post(post)
        await ws.send(str(likes))
        print("sent " + str(likes))

    if command == "postloader":
        f = open("posts_database.json", "r")
        s = f.read()

        await ws.send(s)
        f.close()

    if command == "postuploader":
        post = await ws.recv()
        post = json.loads(post)
        print(post)
        add_post_to_database(post)




start_server = websockets.serve(mainloop, '192.168.1.12', 8080)

asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()  