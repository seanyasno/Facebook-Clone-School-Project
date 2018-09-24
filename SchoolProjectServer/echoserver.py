import asyncio
import websockets

async def hello(websocket, path):
    name = await websocket.recv()
    print(f"< {name}")

    greeting = f"Hello {name}!"

    await websocket.send(greeting)

    msg = await websocket.recv()
    while msg != "quit":
        print("Message received: " + msg)
        await websocket.send("echo " + msg)
        msg = await websocket.recv()

start_server = websockets.serve(hello, '192.168.1.12', 8080)

asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()  