// noinspection JSPotentiallyInvalidUsageOfClassThis

let debounceCount = 0;

let debounceTimer;

function debounce(func, timeout = 100, atMost = 2) {
  if (debounceCount >= atMost) {
    debounceCount = 0;
    clearTimeout(debounceTimer);
    func();
  } else {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      debounceCount = 0;
      func();
    }, timeout);
    debounceCount += 1;
  }
}


function main() {
  let textArea = new TextArea();
  let connection = new Connection(textArea);
  textArea.listen(connection);
}

class TextArea {
  textareaElement = document.getElementById("collab-text");
  textareaOldValue = this.textareaElement.value;
  connection;

  constructor() {
    this.textareaElement.value = "";
  }

  listen(connection) {
    this.connection = connection;
    let self = this;
    let batches = [];

    this.textareaElement.addEventListener("input", function (event) {

      //let result = self.calculateChanges(e.target.selectionStart);
      let deleteLen;
      switch (event.inputType) {
        case "insertText":
          batches.push(self.connection.insert(event.data, event.target.selectionStart - event.data.length));
          break;
        case "deleteContentBackward":
        case "deleteWordBackward":
          deleteLen = self.textareaOldValue.length - event.target.value.length;
          batches.push(self.connection.delete(deleteLen, event.target.selectionStart + deleteLen));
          break;
        case "deleteContentForward":
        case "deleteWordForward":
          deleteLen = self.textareaOldValue.length - event.target.value.length;
          batches.push(self.connection.delete(deleteLen, event.target.selectionStart + deleteLen + 1));
          break;
        case "insertLineBreak":
          batches.push(self.connection.insert("\n", event.target.selectionStart - 1));
          break;
        case "":
          self.connection.refreshContent();
        default:
          console.log("sending replace because i got input type " + event.inputType.toString());
          batches.push(self.connection.replace(self.textareaElement.value));
      }
      self.textareaOldValue = self.textareaElement.value;
      debounce(function () {
        self.connection.sendBatches(batches);
        batches = [];
      }, 400);
    })
  }

  insert(pos, s) {
    this.textareaElement.value = this.textareaElement.value.substr(0, pos) + s + this.textareaElement.value.substr(pos);
  }

  replace(s) {
    this.textareaElement.value = s;
  }

  delete(pos, c) {
    this.textareaElement.value = this.textareaElement.value.substr(0, pos - c) + this.textareaElement.value.substr(pos);
  }

  receivedMessage(msgJson) {
    switch (msgJson.value) {
      case ".ServerMessage$Hello":
        let header = document.getElementById("header");
        header.innerText = header.innerText.replace("...", msgJson.helloInfo.collabName);
        break;
      case ".ServerMessage$ContentOverride":
        this.textareaElement.value = msgJson.content;
        this.textareaOldValue = msgJson.content;
        break;
      case ".ClientMessage$TextEdit":
        switch (msgJson.action.value) {
          case ".TextEditAction$Insert":
            this.insert(msgJson.location, msgJson.action.s);
            break;
          case ".TextEditAction$Replace":
            this.replace(msgJson.action.s);
            break;
          case ".TextEditAction$Delete":
            this.delete(msgJson.location, msgJson.action.c);
            break;
        }
        break;
      case ".ServerMessage$ClientMessagesForwarded":
        msgJson.messages.forEach((msg) => this.receivedMessage(msg));
        if (msgJson.contentHash !== this.textareaElement.value.hashCode()) {
          this.connection.refreshContent()
        }
        break;
    }
  }
}

class Connection {
  constructor(textArea) {
    let collabId = window.location.href.substring(window.location.href.lastIndexOf("/"));
    let protocol = window.location.protocol.replace(/^http(s)?:/, "ws$1:");
    let url = protocol + window.location.host + "/connect" + collabId

    this.websocket = new WebSocket(url);
    this.websocket.onmessage = ((m) => {
      let msgJson = JSON.parse(m.data);
      textArea.receivedMessage(msgJson);
    })
  }

  insert(s, pos) {
    return {
      "value": ".ClientMessage$TextEdit",
      "location": pos,
      "action": {
        "value": ".TextEditAction$Insert",
        "s": s
      }
    }
  }

  replace(s) {
    return {
      "value": ".ClientMessage$TextEdit",
      "location": 0,
      "action": {
        "value": ".TextEditAction$Replace",
        "s": s
      }
    }
  }

  delete(c, pos) {
    return {
      "value": ".ClientMessage$TextEdit",
      "location": pos,
      "action": {
        "value": ".TextEditAction$Delete",
        "c": c
      }
    }
  }

  sendBatches(batches) {
    if (batches.length > 0) {
      this.websocket.send(JSON.stringify(batches));
    }
  }

  refreshContent() {
    this.websocket.send(JSON.stringify([{"value": ".ClientMessage$Refresh"}]));
  }
}

String.prototype.hashCode = function () {
  let hash = 0;
  if (this.length === 0) {
    return hash;
  }
  for (let i = 0; i < this.length; i++) {
    const char = this.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32bit integer
  }
  return hash;
}

main()