const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
    .then(function (response) {
        return response.json();
    })
    .then(function (serviceList) {
        serviceList.forEach(service = > {
            var li = document.createElement("li");
        var button = document.createElement("button");

        li.appendChild(document.createTextNode(service.name + ': ' + service.status));
        li.id = listContainer.length;
        button.id = 'btn-' + listContainer.length;
        button.value = 'Remove';
        button.addEventListener("click", removeService);
        li.appendChild(button);
        listContainer.appendChild(li);
    })
        ;
    });

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt =
>
{
    let urlName = document.querySelector('#url-name').value;
    fetch('/service', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({url: urlName})
    }).then(res = > location.reload()
)
    ;
}

function removeService(event) {
    listContainer.removeChild(event.target.parentNode);
}
