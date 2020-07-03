const slidingDivs = document.querySelectorAll(".slide-in");

//add scroll event (the divs will appear on the window when it's on viewport)
window.addEventListener("scroll", function(event){
    slidingDivs.forEach(slidingDiv => {
        const halfWayThrough = window.innerHeight + window.scrollY - slidingDiv.clientHeight / 2;
        const isHalfWayThrough = halfWayThrough > slidingDiv.offsetTop;
        const imageBottom = slidingDiv.offsetTop + slidingDiv.clientHeight;
        const isNotPassed = imageBottom > window.scrollY;

        if(isHalfWayThrough && isNotPassed){
            slidingDiv.classList.add("active");
        }
        else{
            slidingDiv.classList.remove("active");
        }
    });
});

const helloElement = document.querySelector(".typing");
//add "typing" feature for h1 tag "Hello" on index.html page

var i = 0;
var text = "Hello";
var timeOutTime = 50;
var typer = true;
var transition = true;

function typing(){
    if(i < text.length){
        if(typer){
            helloElement.innerHTML = "|";
            typer = false;
            setTimeout(typing, 500);
        }
        else{
            if(transition){
                helloElement.innerHTML = text.charAt(i);
                transition = false;
                i++;
            }
            else{
                helloElement.innerHTML += text.charAt(i);
                i++;
            }
            setTimeout(typing, timeOutTime);
        }
    }
    else{
        if(!typer){
            helloElement.innerHTML += "|";
            typer = true;
        }
        else{
            helloElement.innerHTML = helloElement.innerText.slice(0,helloElement.innerText.length - 1);
            typer = false;
        }
        setTimeout(typing, 400);
    }
};