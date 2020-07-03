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