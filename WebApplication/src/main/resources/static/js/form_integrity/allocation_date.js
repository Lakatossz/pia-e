/**
 * Controller for integrity check in form consisting of inputs for start-end date.
 */
class AllocationDateController {

    #date_from;
    #date_to;

    /**
     * @param date_from date from input id
     * @param date_to date to input id
     */
    constructor(date_from, date_to) {
        this.#date_from = '#' + date_from;
        this.#date_to = '#' + date_to;

        this.#setupInputs();
    }

    #setupInputs() {
        let content = this;

        $(this.#date_from).on('change', function () {
            content.#setMinIntegrity();
        });
        $(this.#date_to).on('change', function () {
            content.#setMaxIntegrity();
        });
    }

    #setMaxIntegrity() {
        let maxValue = $(this.#date_to).val();
        $(this.#date_from).attr('max', maxValue);
    }

    #setMinIntegrity() {
        let minValue = $(this.#date_from).val();
        $(this.#date_to).attr('min', minValue);
    }
}