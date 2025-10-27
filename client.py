import xmlrpc.client, sys

name = "http://"+sys.argv[1]+":8888"

server = xmlrpc.client.ServerProxy(name)

while True:
  query = input("Available commands: buy[id], search[topic], lookup[id], exit \n")

  if query == "exit":
    exit()

  inputSplit = query.split(" ", 1)

  if inputSplit[0] == "buy":
      if len(inputSplit) == 2:
        print(server.sample.buy(int(inputSplit[1])))

  if inputSplit[0] == "lookup":
      if len(inputSplit) == 2: 
        print(server.sample.lookup(int(inputSplit[1])))

  if inputSplit[0] == "search":
      if len(inputSplit) == 2: 
        print(server.sample.search(inputSplit[1]))


