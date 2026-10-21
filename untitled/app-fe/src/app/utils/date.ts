/** Calendar date in the same timezone used by the backend, suitable for DatePicker. */
export function todayDate(): Date {
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Europe/Warsaw', year: 'numeric', month: '2-digit', day: '2-digit'
  }).formatToParts(new Date());
  const part = (type: Intl.DateTimeFormatPartTypes): number =>
    Number(parts.find(value => value.type === type)!.value);
  return new Date(part('year'), part('month') - 1, part('day'));
}
