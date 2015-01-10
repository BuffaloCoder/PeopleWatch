from flask import Flask, jsonify, request

app = Flask(__name__)

places = [None] * 4 # location of user

@app.route('/')
def hello():
    return 'Hello World!'

@app.route('/update')
def update():
	user = request.args.get('user')
	location = request.args.get('location')
	userId = int(user)
	places[userId] = location
	return 'User: ' + user + ' | Location: ' + location

@app.route('/locations')
def locations():
	json = jsonify(user0 = places[0],
                   user1 = places[1],
                   user2 = places[2],
                   user3 = places[3])
	return json

if __name__ == '__main__':
    app.run(debug=True)