document.getElementById("create-collab-form").onsubmit = createCollab;
const errorText = document.getElementById("create-collab-error");
const collabName = document.getElementById("create-collab-name");
const collabDuration = document.getElementById("create-collab-duration");

let urlParams = new URLSearchParams(window.location.search);
if (urlParams.get("id") !== null) {
  collabName.value = urlParams.get("id");
  window.history.replaceState(null, null, "/");
} else {
  collabName.value = "";
}

async function createCollab(event) {
  errorText.hidden = true;
  event.preventDefault();
  event.stopPropagation();

  const response = await fetch("/collabs/" + collabName.value, {
    method: "POST",
    body: JSON.stringify({duration: collabDuration}),
    headers: {"Content-Type": "application/json"}
  });
  if (response.ok === true) {
    window.location.href = "/collabs/" + collabName.value;
  } else {
    errorText.hidden = false;
  }


  return false;
}
