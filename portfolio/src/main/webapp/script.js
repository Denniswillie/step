function fetchFromData(){
    fetch('/data').then(response => response.json()).then((commentsList) => {
        const scrollElement = document.querySelector(".specialScroll");
        commentsList.forEach(comment => {
            const ticketsDiv = document.createElement("div");
            const ticketsRightDiv = document.createElement("div");
            const ticketsLeftDiv = document.createElement("div");
            const h3Element = document.createElement("h3");
            const h4Element = document.createElement("h4");
            ticketsDiv.setAttribute('class', 'tickets');
            ticketsRightDiv.setAttribute('class', 'ticketsRight');
            ticketsLeftDiv.setAttribute('class', 'ticketsLeft');
            ticketsRightDiv.innerText = comment.comment;
            h3Element.innerText = comment.name;
            h4Element.innerText = comment.relationship;
            ticketsLeftDiv.appendChild(h3Element);
            ticketsLeftDiv.appendChild(h4Element);
            ticketsDiv.appendChild(ticketsRightDiv);
            ticketsDiv.appendChild(ticketsLeftDiv);
            scrollElement.appendChild(ticketsDiv);
        });
    });
}