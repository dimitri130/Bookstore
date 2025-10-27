import xmlrpc.client, sys


with xmlrpc.client.ServerProxy("http://"+sys.argv[1]+":8888") as proxy:

    i=0
    j=0

#    while i<500:
#        print(proxy.sample.search("college life"))
#        i += 1

    while j<500:
        print(proxy.sample.buy(12365))
        j += 1
    
