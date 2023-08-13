import { WebSocketServer } from 'ws';

const wss = new WebSocketServer({ port: 8080 });

let currentAction = 'idle'

wss.on('connection', function connection(ws) {
    ws.on('message', function message(data) {
        console.log('received: %s', data);
        const messageBody = JSON.parse(data);
        if (messageBody.action != null) {
            currentAction = messageBody.action
        }
    });
    
    setInterval(async () => {
        ws.send(currentAction)
    }, 1000);

    ws.send('accepted');
});